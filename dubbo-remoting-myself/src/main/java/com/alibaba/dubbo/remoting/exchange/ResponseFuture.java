package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.RemotingException;

/**
 * Future. (API/SPI, Prototype, ThreadSafe)
 * @author wangyu
 *
 */
public interface ResponseFuture {

    
    Object get() throws RemotingException;
    
    
    Object get(int timeoutInMillis) throws RemotingException;
    
    
    void setCallback(ResponseCallback callback);
    
    boolean isDone();
}
