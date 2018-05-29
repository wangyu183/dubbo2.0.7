package com.alibaba.dubbo.rpc.cluster;

import java.util.List;

import com.alibaba.dubbo.common.Node;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * Directory. (SPI, Singleton, ThreadSafe)
 * @author wangyu
 *
 * @param <T>
 */
public interface Directory<T> extends Node {

    Class<T> getInterface();
    
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
    
}
