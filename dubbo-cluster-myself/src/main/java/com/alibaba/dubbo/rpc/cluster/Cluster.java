package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * SPI.Singleton.ThreadSafe
 * @author wangyu
 *
 */
public interface Cluster {

    <T> Invoker<T> merge(Directory<T> directory) throws RpcException;
    
}
