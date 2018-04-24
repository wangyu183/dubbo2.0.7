package com.alibaba.dubbo.rpc.support;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

public abstract class AbstractExporter<T> implements Exporter<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Invoker<T> invoker;
    
    private volatile boolean unexported = false;
    
    public AbstractExporter(Invoker<T> invoker) {
        if(invoker == null) {
            throw new IllegalStateException("service invoker == null");
        }
        if(invoker.getInterface() == null) {
            throw new IllegalStateException("service type == null");
        }
        if(invoker.getUrl() == null) {
            throw new IllegalStateException("service url == null");
        }
        this.invoker = invoker;
    }
    
    public Invoker<T> getInvoker() {
        return invoker;
    }
    
    public void unexport() {
        if (unexported)
            throw new IllegalStateException("The exporter " + this + " unexported!");
        unexported = true;
        getInvoker().destroy();
    }
    
    public String toString() {
        return getInvoker().toString();
    }
    
    public Result invoke(Invocation invocation,InetSocketAddress remoteAddress) throws RpcException{
        RpcContext.getContext().setRemoteAddress(remoteAddress);
        return getInvoker().invoke(invocation);
    }
    
    public Result invoke(Invocation invocation, String remoteHost, int remotePort) throws RpcException {
        if (remoteHost != null && remoteHost.length() > 0) {
            if (remotePort < 0) {
                remotePort = 0;
            }
            RpcContext.getContext().setRemoteAddress(remoteHost, remotePort);
        }
        return getInvoker().invoke(invocation);
    }
}
