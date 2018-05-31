package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

@Extension(FailfastCluster.NAME)
public class FailfastCluster implements Cluster {
    
    public final static String NAME = "failfast";
    
    public <T> Invoker<T> merge(Directory<T> directory) throws RpcException {
        return new FailfastClusterInvoker<T>(directory);
    }

}
