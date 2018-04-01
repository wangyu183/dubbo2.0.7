package com.alibaba.dubbo.remoting;

public interface ChannelHandler {
    
    void connected(Channel channel) throws RemotingException;
    
    void disconnected(Channel channel) throws RemotingException;
    
    void sent(Channel channel,Object message) throws RemotingException;
    
    void reveived(Channel channel,Object message) throws RemotingException;
    
    void caught(Channel channel,Throwable exception) throws RemotingException;
    
}