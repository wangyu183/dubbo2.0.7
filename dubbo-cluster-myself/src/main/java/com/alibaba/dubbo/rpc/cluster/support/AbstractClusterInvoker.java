package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcConstants;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

public abstract class AbstractClusterInvoker<T> implements Invoker<T> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final Directory<T> directory;
    
    protected final boolean availablecheck;
    
    private volatile boolean destroyed = false;
    
    private volatile Invoker<T> stickyInvoker = null;
    
    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }
    
    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if(directory == null) {
            throw new IllegalArgumentException("service directory == null");
        }
        this.directory = directory;
        this.availablecheck = url.getBooleanParameter(RpcConstants.CLUSTER_AVAILABLE_CHECK_KEY, RpcConstants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
    }
    
    public Class<T> getInterface() {
        return null;
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
        if(destroyed) {
            throw new RpcException("Rpc invoker for " + getInterface() + " on consumer " + NetUtils.getLocalHost() 
            + " use dubbo version " + Version.getVersion()
            + " is not destroyed! Can not invoke any more.");
        }
        LoadBalance loadbalance;
        //得到路由后的invokers
        List<Invoker<T>> invokers = directory.list(invocation);
        if(invokers != null && invokers.size() > 0) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl().getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        }else {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
        return doInvoke(invocation, invokers, loadbalance);
    }

    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException;
    
    public void destroy() {
        directory.destroy();
        destroyed = true;
    }
    
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException{
        if(invokers == null || invokers.size() == 0) {
            return null;
        }
        String methodName = invocation == null ? "" : invocation.getMethodName();
        boolean sticky = invokers.get(0).getUrl().getMethodBooleanParameter(methodName, RpcConstants.CLUSTER_STICKY_KEY, RpcConstants.DEFAULT_CLUSTER_STICKY);
        {
            if(stickyInvoker != null && !invokers.contains(stickyInvoker)) {
                stickyInvoker = null;
            }
          //ignore cucurrent problem
            if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))){
                if (availablecheck && stickyInvoker.isAvailable()){
                    return stickyInvoker;
                }
            }
        }
        Invoker<T> invoker = doselect(loadbalance, invocation, invokers, selected);
        if(sticky) {
            stickyInvoker = invoker;
        }
        return invoker;
    }
    
    private Invoker<T> doselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException{
        if(invokers == null || invokers.size() == 0) {
            return null;
        }
        if(invokers.size() == 1) {
            return invokers.get(0);
        }
        if(invokers.size() == 2 && selected != null && selected.size() > 0) {
            return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
        }
        Invoker<T> invoker = loadbalance.select(invokers, invocation);
        if((selected != null && selected.contains(invoker))
                ||(!invoker.isAvailable() && getUrl() != null && availablecheck)){
            try {
              Invoker<T> rinvoker = reselect(loadbalance, invocation, invokers, selected, availablecheck);  
              if(rinvoker != null) {
                  invoker = rinvoker;
              }else {
                  int index = invokers.indexOf(invoker);
                  try {
                    invoker = index < invokers.size() - 1 ? invokers.get(index + 1) : invoker;
                } catch (Exception e) {
                    logger.warn(e.getMessage()+" may because invokers list dynamic change, ignore.",e);
                }
              }
            } catch (Throwable t) {
                logger.error("clustor reselect fail reason is :" + t.getMessage());
            }
        }
        return invoker;
    }
    
    private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availablecheck){
        List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>(invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());
        if(availablecheck) {
            for(Invoker<T> invoker : invokers) {
                if(invoker.isAvailable()) {
                    if(selected == null || !selected.contains(invoker)) {
                        reselectInvokers.add(invoker);
                    }
                }
                if(reselectInvokers.size() > 0) {
                    return loadbalance.select(reselectInvokers, invocation);
                }
            }
        }else {
            for(Invoker<T> invoker : invokers){
                if(selected ==null || !selected.contains(invoker)){
                    reselectInvokers.add(invoker);
                }
            }
            if(reselectInvokers.size()>0){
                return  loadbalance.select(reselectInvokers, invocation);
            }
        }
        {
            if(selected != null){
                for(Invoker<T> invoker : selected){
                    if((invoker.isAvailable()) //优先选available 
                            && !reselectInvokers.contains(invoker)){
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if(reselectInvokers.size()>0){
                return  loadbalance.select(reselectInvokers, invocation);
            }
        }
        return null;
    }
}
