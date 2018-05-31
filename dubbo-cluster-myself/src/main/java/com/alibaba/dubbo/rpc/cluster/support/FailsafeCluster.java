package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

/**
 * 失败安全，出现异常直接忽略
 * @author wangyu
 *
 */
@Extension(FailsafeCluster.NAME)
public class FailsafeCluster implements Cluster {
    
    public static final String NAME = "failsafe";

    public <T> Invoker<T> merge(Directory<T> directory) throws RpcException {
        // TODO Auto-generated method stub
        return null;
    }

}
