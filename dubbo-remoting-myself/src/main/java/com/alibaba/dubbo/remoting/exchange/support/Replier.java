package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;

public interface Replier<T> {
    
    Object reply(ExchangeChannel channel, T request) throws RemotingException;
}
