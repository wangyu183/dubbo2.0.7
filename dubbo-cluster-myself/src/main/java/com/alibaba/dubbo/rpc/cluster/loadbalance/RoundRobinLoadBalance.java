package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

@Extension(RoundRobinLoadBalance.NAME)
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "roundrobin";
    
    private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();
    
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, Invocation invocation) {
        String key = invokers.get(0).getInterface() + "." + invocation.getMethodName();
        AtomicPositiveInteger sequence = sequences.get(key);
        if(sequence == null) {
            sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = sequences.get(key);
        }
        return invokers.get(sequence.getAndIncrement() % invokers.size());
    }

}
