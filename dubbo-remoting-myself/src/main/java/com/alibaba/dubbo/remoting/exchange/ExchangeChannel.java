package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;

/**
 * ExchangeChannel. (API/SPI, Prototype, ThreadSafe)
 * @author wangyu
 *
 */
public interface ExchangeChannel extends Channel {

    ResponseFuture request(Object request) throws RemotingException;
    
    ResponseFuture request(Object request,int timeout) throws RemotingException;
    
    void close(int timeout);
    
    ExchangeHandler getExchangeHandler();
    
}
