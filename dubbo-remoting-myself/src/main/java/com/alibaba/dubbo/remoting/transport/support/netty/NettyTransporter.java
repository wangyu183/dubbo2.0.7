package com.alibaba.dubbo.remoting.transport.support.netty;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;

@Extension(NettyTransporter.NAME)
public class NettyTransporter implements Transporter {
    public static final String NAME = "netty";

    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        return new NettyClient(url, handler);
    }

    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new NettyServer(url, handler);
    }
    
    
}
