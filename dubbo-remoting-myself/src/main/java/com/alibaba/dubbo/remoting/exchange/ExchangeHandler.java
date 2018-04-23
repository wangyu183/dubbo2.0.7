package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Telnet.TelnetHandler;

public interface ExchangeHandler extends ChannelHandler, TelnetHandler {

    Object reply(ExchangeChannel channel, Object request) throws RemotingException;
}
