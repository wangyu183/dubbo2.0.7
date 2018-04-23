package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;

/**
 * Invoker.(API/SPI,Prototype,ThreadSafe)
 * @author wangyu
 *
 * @param <T>
 */
public interface Invoker<T> {
    
    /**
     * get service interface.
     * @return
     */
    Class<T> getInterface();
    
    /**
     * get service url.
     * @return
     */
    URL getUrl();
    
    boolean isAvailable();
    
    Result invoke(Invocation invocation) throws RpcException;
    
    void destroy();
    
    
}
