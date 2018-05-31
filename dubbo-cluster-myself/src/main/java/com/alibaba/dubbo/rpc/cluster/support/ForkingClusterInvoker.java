package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

public class ForkingClusterInvoker<T> extends AbstractClusterInvoker<T> {
    
    private final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("forking-cluster-timer", true));
    
    public ForkingClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(final Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
            throws RpcException {
        final List<Invoker<T>> selected;
        final int forks = getUrl().getIntParameter(Constants.FORKS_KEY, Constants.DEFAULT_FORKS);
        final int timeout = getUrl().getIntParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        if(forks <= 0 || forks >= invokers.size()) {
            selected = invokers;
        }else {
            selected = new ArrayList<Invoker<T>>();
            for(int i = 0; i < forks; i++) {
                Invoker<T> invoker = select(loadbalance, invocation, invokers, selected);
                if(!selected.contains(invoker)) {
                    selected.add(invoker);
                }
            }
        }
        final AtomicInteger count = new AtomicInteger();
        final BlockingQueue<Result> ref = new LinkedBlockingQueue<Result>();
        for(final Invoker<T> invoker : selected) {
            executor.execute(new Runnable() {
                
                public void run() {
                    try {
                        //TODO 为啥invocation必须是final
                        Result result = invoker.invoke(invocation);
                        ref.offer(result);
                    } catch (Throwable e) {
                        int value = count.incrementAndGet();
                        if(value >= selected.size()) {
                            ref.offer(new RpcResult(e));
                        }
                    }
                }
            });
        }
        try {
            return ref.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RpcException("Failed to forking invoke provider " + selected + ", cause: " + e.getMessage(), e);
        }
    }

}
