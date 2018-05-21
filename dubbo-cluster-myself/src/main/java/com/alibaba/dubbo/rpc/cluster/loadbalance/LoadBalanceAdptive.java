package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

@Adaptive
public class LoadBalanceAdptive implements LoadBalance {

    public <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) throws RpcException {
        if(invokers == null || invokers.size() == 0) {
            return null;
        }
        URL url = invokers.get(0).getUrl();
        String method = invocation.getMethodName();
        String name;
        if(method == null || method.length() == 0) {
            name = url.getParameter(Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE);
        }else {
            name = url.getMethodParameter(method, Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE);
        }
        LoadBalance loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(name);
        return loadbalance.select(invokers, invocation);
    }
}
