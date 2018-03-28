package com.alibaba.dubbo.remoting.transport.support;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Resetable;

public abstract class AbstractEndpoint extends AbstractPeer implements Resetable{
    
    private Codec codec;
    
    private int timeout;
    
    private int connectTimeout;
    
    
    public AbstractEndpoint(URL url, ChannelHandler handler) {
        super(url, handler);
        this.codec = ExtensionLoader.getExtensionLoader(Codec.class).getExtension(url.getParameter(Constants.CODEC_KEY, "telnet"));
        
        // TODO Auto-generated constructor stub
    }

    public InetSocketAddress getLocalAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    public void send(Object message, boolean sent) throws RemotingException {
        // TODO Auto-generated method stub

    }

}
