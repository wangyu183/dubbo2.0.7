package com.alibaba.dubbo.rpc.cluster.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

public class FailbackClusterInvoker<T> extends AbstractClusterInvoker<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(FailbackClusterInvoker.class);
    
    private static final long RETRY_FAILED_PERIOD = 5 * 1000;
    
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new NamedThreadFactory("failback-cluster-timer", true)); 
    
    private volatile ScheduledFuture<?> retryFuture;
    
    private final ConcurrentMap<Invocation, AbstractClusterInvoker<?>> failed = new ConcurrentHashMap<Invocation, AbstractClusterInvoker<?>>();
    
    public FailbackClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
            throws RpcException {
        Invoker<T> invoker = select(loadbalance, invocation, invokers, null);
        try {
            return invoker.invoke(invocation);
        } catch (Throwable e) {
            logger.error("Failback to invoke " + invocation + ", wait for retry in background. Ignored exception: " + e.getMessage(), e);
            addFailed(invocation, this);
            return new RpcResult();
        }
    }
    
    private void addFailed(Invocation invocation, AbstractClusterInvoker<?> router) {
        if(retryFuture == null) {
            synchronized (this) {
                if(retryFuture == null) {
                    retryFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                        
                        public void run() {
                            try {
                                retryFailed();
                            } catch (Throwable t) {
                                logger.error("Unexpected error occur at collect statistic", t);
                            }
                        }
                    }, RETRY_FAILED_PERIOD, RETRY_FAILED_PERIOD, TimeUnit.MILLISECONDS);
                }
            }
        }
        failed.put(invocation, router);
    }
    
    void retryFailed() {
        if(failed.size() == 0) {
            return;
        }
        for(Map.Entry<Invocation, AbstractClusterInvoker<?>> entry : new HashMap<Invocation, AbstractClusterInvoker<?>>(failed).entrySet()) {
            Invocation invocation = entry.getKey();
            Invoker<?> invoker = entry.getValue();
            try {
                invoker.invoke(invocation);
                failed.remove(invocation);
            } catch (Throwable e) {
                logger.error("Failed retry to invoke" + invocation + ", waiting again.", e);
            }
        }
    }

}
