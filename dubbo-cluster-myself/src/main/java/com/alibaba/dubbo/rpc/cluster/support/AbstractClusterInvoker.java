package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcConstants;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;

public class AbstractClusterInvoker<T> implements Invoker<T> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final Directory<T> directory;
    
    protected final boolean availablecheck;
    
    private volatile boolean destroyed = false;
    
    private volatile Invoker<T> stickyInvoker = null;
    
    public Class<T> getInterface() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if(directory == null) {
            throw new IllegalArgumentException("service directory == null");
        }
        this.directory = directory;
        this.availablecheck = url.getBooleanParameter(RpcConstants.CLUSTER_AVAILABLE_CHECK_KEY, RpcConstants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
    }

    public URL getUrl() {
        return directory.getUrl();
    }

    public boolean isAvailable() {
        Invoker<T> invoker = stickyInvoker;
        if(invoker != null) {
            return invoker.isAvailable();
        }
        return directory.isAvailable();
    }

    public Result invoke(Invocation invocation) throws RpcException {
        // TODO Auto-generated method stub
        return null;
    }

    public void destroy() {
        directory.destroy();
        destroyed = true;
    }

}
