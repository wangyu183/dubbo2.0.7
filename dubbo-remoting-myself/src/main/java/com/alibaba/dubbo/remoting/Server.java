package com.alibaba.dubbo.remoting;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends Endpoint,Resetable {
    
    boolean isBound();
    
    Collection<Channel> getChannels();
    
    Channel getChannel(InetSocketAddress remoteAddress);
    
}
