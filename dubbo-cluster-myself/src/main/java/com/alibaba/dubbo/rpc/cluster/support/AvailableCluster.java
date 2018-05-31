package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

@Extension(AvailableCluster.NAME)
public class AvailableCluster implements Cluster {
    
    public static final String NAME = "available";
    
    public <T> Invoker<T> merge(Directory<T> directory) throws RpcException {
        return new AvailableClusterInvoker<T>(directory);
    }

}
