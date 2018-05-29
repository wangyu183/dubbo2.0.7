package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

public abstract class AbstractDirectory<T> implements Directory<T> {

    private final URL url;
    private Boolean destroyed = false;
    private List<Router> routers = new ArrayList<Router>();
    
    public AbstractDirectory(URL url) {
        this(url, null);
    }
    
    public AbstractDirectory(URL url, List<Router> routers) {
        if(url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if(routers == null) {
            routers = new ArrayList<Router>();
        }
        this.url = url;
        String routerkey = url.getParameter(Constants.ROUTER_KEY);
        if(routerkey != null && routerkey.length() > 0) {
            RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(routerkey);
            routers.add(routerFactory.getRouter(url));
        }
        if(routers != null) {
            setRouters(routers);
        }
    }
    
    protected void setRouters(List<Router> r) {
        routers = r;
    }

    public List<Router> getRouters(){
        return routers;
    }

    public URL getUrl() {
        return url;
    }

    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        List<Invoker<T>> invokers = doList(invocation);
        for(Router router : routers) {
            invokers = router.route(invokers, invocation);
        }
        return invokers;
    }
    
    protected abstract List<Invoker<T>> doList(Invocation invocation) throws RpcException;

}
