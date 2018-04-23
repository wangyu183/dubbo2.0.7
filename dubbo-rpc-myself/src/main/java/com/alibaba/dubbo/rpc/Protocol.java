package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;

/**
 * Protocol. (SPI,Singleton,ThreadSafe)
 * @author wangyu
 *
 */
@Extension("dubbo")
public interface Protocol {
    
    int getDefaultPort();
    
    @Adaptive
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;

    @Adaptive
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;
    
    void destory();
}
