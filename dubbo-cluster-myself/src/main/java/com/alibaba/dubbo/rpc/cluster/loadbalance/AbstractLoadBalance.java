package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

public abstract class AbstractLoadBalance implements LoadBalance {

    public <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) throws RpcException {
        if(invokers == null || invokers.size() == 0) {
            return null;
        }
        if(invokers.size() == 1) {
            return invokers.get(0);
        }
        return doSelect(invokers, invocation);
    }
    
    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation);
    
    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        return invoker.getUrl().getMethodIntParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
    }
}
