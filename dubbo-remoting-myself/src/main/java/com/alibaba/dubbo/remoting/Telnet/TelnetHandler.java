package com.alibaba.dubbo.remoting.Telnet;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;

@Extension
public interface TelnetHandler {

    String telnet(Channel channel, String message) throws RemotingException;
}
