package com.alibaba.dubbo.remoting.transport.support;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Endpoint;
import com.alibaba.dubbo.remoting.RemotingException;

public abstract class AbstractPeer implements Endpoint, ChannelHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ChannelHandler handler;
    
    private volatile URL url;
    
    private volatile boolean closed;
    
    public AbstractPeer(URL url,ChannelHandler handler) {
        if(url == null) {
            throw new IllegalArgumentException("url == null");
        }
        
        if(handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        
        this.url = url;
        this.handler = handler;
    }
    
    public void connected(Channel channel) throws RemotingException {
        if(closed) {
            return;
        }
        handler.connected(channel);
    }

    public void disconnected(Channel channel) throws RemotingException {
        handler.disconnected(channel);

    }


    public void reveived(Channel channel, Object message) throws RemotingException {
        // TODO Auto-generated method stub
        if(closed) {
            return;
        }
        handler.reveived(channel, message);

    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        // TODO Auto-generated method stub

    }

    public URL getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public ChannelHandler getChannelHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public InetSocketAddress getLocalAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    public void send(Object message) throws RemotingException {
        send(message,url.getBooleanParameter(Constants.SENT_KEY));
    }

    public void send(Object message, boolean sent) throws RemotingException {
        // TODO Auto-generated method stub

    }

    public void close() {
        // TODO Auto-generated method stub

    }

    public void close(int timeout) {
        // TODO Auto-generated method stub

    }

    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

}
