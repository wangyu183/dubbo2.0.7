package com.alibaba.dubbo.rpc.cluster;

import java.util.List;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

@Extension
public interface LoadBalance {
    
    /**
     * select one invoker in list.
     * 
     * @param invokers
     * @param invocation
     * @return
     * @throws RpcException
     */
    <T> Invoker<T> select(List<Invoker<T>> invokers, Invocation invocation) throws RpcException;
}
