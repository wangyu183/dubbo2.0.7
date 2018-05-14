package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.Extension;

@Extension
public interface Filter {
    
    Result invoke(Invoker<?> invoker, Invocation invacation) throws RpcException;

}
