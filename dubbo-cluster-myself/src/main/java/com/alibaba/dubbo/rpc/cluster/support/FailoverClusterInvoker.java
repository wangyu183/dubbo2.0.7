package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public FailoverClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
            throws RpcException {
        if(invokers == null || invokers.size() == 0) {
            throw new RpcException("No provider available for service" + getInterface().getName() + "on consumer " + NetUtils.getLocalHost() + "use dubbo version " + Version.getVersion() + ", Please check whether the service do exist or version is right firstly, and check the provider has started.");
        }
        int len = getUrl().getMethodIntParameter(invocation.getMethodName(), Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES) + 1;
        if(len <= 0) {
            len = 1;
        }
        Throwable le = null;
        List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(invokers.size());
        Set<URL> providers = new HashSet<URL>(len);
        for(int i = 0; i < len; i++) {
            Invoker<T> invoker = select(loadbalance, invocation, invokers, invoked);
            invoked.add(invoker);
            providers.add(invoker.getUrl());
            try {
                return invoker.invoke(invocation);
            } catch (RpcException e) {
                if (e.isBiz()) throw e;

                le = e;
                //pp = true;
            } catch (Throwable e) // biz exception.
            {
                throw new RpcException(e.getMessage(), e);
            } finally {
                //if (pp) // if provider problem, fail over.
                //    inv.setWeight(0);
            }
        }
        List<URL> urls = new ArrayList<URL>(invokers.size());
        for(Invoker<T> invoker : invokers){
            if(invoker != null ) 
                urls.add(invoker.getUrl());
        }
        throw new RpcException("Tried " + len + " times to invoke providers " + providers + " " + loadbalance.getClass().getAnnotation(Extension.class).value() + " select from all providers " + invokers + " for service " + getInterface().getName() + " method " + invocation.getMethodName() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", but no luck to perform the invocation. Last error is: " + (le != null ? le.getMessage() : ""), le);
    
    }

}
