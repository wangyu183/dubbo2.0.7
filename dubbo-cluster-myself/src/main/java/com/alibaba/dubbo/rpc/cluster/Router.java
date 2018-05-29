package com.alibaba.dubbo.rpc.cluster;

import java.util.List;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

public interface Router {

    
    <T> List<Invoker<T>> route(List<Invoker<T>> invokers, Invocation invocation);
}
