package com.alibaba.dubbo.remoting.exchange;

import java.net.InetSocketAddress;
import java.util.Collection;

import com.alibaba.dubbo.remoting.Server;

/**
 * ExchangeServer. (API/SPI, Prototype, ThreadSafe)
 * @author wangyu
 *
 */
public interface ExchangeServer extends Server{

    Collection<ExchangeChannel> getExchangeChannels();
    
    ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress);
}
