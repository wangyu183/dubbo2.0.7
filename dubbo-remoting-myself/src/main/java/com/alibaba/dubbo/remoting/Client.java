package com.alibaba.dubbo.remoting;


/**
 * Remoting Client. (API/SPI, Prototype, ThreadSafe)
 * @author wangyu
 *
 */
public interface Client extends Endpoint, Channel, Resetable {
    
    void reconnect() throws RemotingException;
    
}
