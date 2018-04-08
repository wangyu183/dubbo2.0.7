package com.alibaba.dubbo.remoting.transport.support;

import java.io.IOException;

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
        if(closed) {
            return;
        }
        handler.received(channel, message);

    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        if(exception instanceof IOException || exception instanceof RemotingException) {
            logger.warn("IOException on channel " + channel, exception);
        }else {
            logger.error("Exception on channel " + channel,exception);
        }
        handler.caught(channel, exception);

    }
    
    public void sent(Channel ch, Object msg) throws RemotingException {
        if (closed) {
            return;
        }
        handler.sent(ch, msg);
    }

    public URL getUrl() {
        return url;
    }
    
    protected void setUrl(URL url) {
        if(url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
    }

    public ChannelHandler getChannelHandler() {
        return handler;
    }

    public void send(Object message) throws RemotingException {
        send(message,url.getBooleanParameter(Constants.SENT_KEY));
    }


    public void close() {
        closed = true;

    }

    public void close(int timeout) {
        close();

    }

    public boolean isClosed() {
        return closed;
    }

}
