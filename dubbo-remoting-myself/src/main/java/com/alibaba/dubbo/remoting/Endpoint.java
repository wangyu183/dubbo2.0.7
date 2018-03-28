package com.alibaba.dubbo.remoting;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.URL;

public interface Endpoint {
    
    URL getUrl();
    
    ChannelHandler getChannelHandler();
    
    InetSocketAddress getLocalAddress();
    
    void send(Object message) throws RemotingException;
    
    /**
     * 
     * @param message
     * @param sent 是否已经发送完成
     * @throws RemotingException
     */
    void send(Object message,boolean sent) throws RemotingException;
    
    void close();
    
    void close(int timeout);
    
    boolean isClosed();
    
    
    
}
