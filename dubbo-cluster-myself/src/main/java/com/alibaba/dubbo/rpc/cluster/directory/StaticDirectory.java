package com.alibaba.dubbo.rpc.cluster.directory;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.support.AbstractDirectory;

public class StaticDirectory<T> extends AbstractDirectory<T> {
    
    private final List<Invoker<T>> invokers;
    
    private static final List<Invoker<?>> EMPTY_INVOKERLIST = new ArrayList<Invoker<?>>();
    
    public StaticDirectory(List<Invoker<T>> invokers) {
        this(null, invokers, null);
    }
    
    public StaticDirectory(List<Invoker<T>> invokers, List<Router> router) {
        this(null, invokers, router);
    }
    
    public StaticDirectory(URL url, List<Invoker<T>> invokers) {
        this(url, invokers, null);
    }
    
    public StaticDirectory(URL url, List<Invoker<T>> invokers, List<Router> routers) {
        super(url == null && invokers != null && invokers.size() > 0 ? invokers.get(0).getUrl() : url, routers);
        if(invokers == null || invokers.size() == 0) {
            throw new IllegalArgumentException("invokers == null");
        }
        this.invokers = invokers;
    }

    public Class<T> getInterface() {
        return invokers.get(0).getInterface();
    }

    public boolean isAvailable() {
        for(Invoker<T> invoker : invokers) {
            if(invoker.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        for(Invoker<T> invoker : invokers) {
            invoker.destroy();
        }
        invokers.clear();
    }

    @Override
    protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {
        return invokers;
    }

}
