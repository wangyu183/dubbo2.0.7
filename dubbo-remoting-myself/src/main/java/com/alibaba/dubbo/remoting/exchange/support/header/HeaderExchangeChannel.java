package com.alibaba.dubbo.remoting.exchange.support.header;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;

public final class HeaderExchangeChannel implements ExchangeChannel {
    private static final Logger logger      = LoggerFactory.getLogger(HeaderExchangeChannel.class);
    
    private final Channel channel;
    
    private volatile boolean closed = false;
    
     HeaderExchangeChannel(Channel channel){
        if(channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        this.channel = channel;
    }
    
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public boolean hasAttribute(String key) {
        return channel.hasAttribute(key);
    }

    public Object getAttribute(String key) {
        return channel.getAttribute(key);
    }

    public void setAttribute(String key, Object value) {
        channel.setAttribute(key, value);
    }

    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }

    public URL getUrl() {
        return channel.getUrl();
    }

    public ChannelHandler getChannelHandler() {
        return channel.getChannelHandler();
    }

    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
    }

    public void send(Object message) throws RemotingException {
        send(message, getUrl().getBooleanParameter(Constants.SENT_KEY));
    }

    public void send(Object message, boolean sent) throws RemotingException {
        if(closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The channel " + this + " is closed!");
        }
        if(message instanceof Request
                || message instanceof Response
                || message instanceof String) {
            channel.send(message,sent);
        }else {
            Request request = new Request();
            request.setVersion("2.0.0");
            request.setTwoWay(false);
            request.setData(message);
            channel.send(request, sent);
        }
        
    }

    public void close() {
        try {
            channel.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public ResponseFuture request(Object request) throws RemotingException {
        return request(request, channel.getUrl().getPositiveIntParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
    }

    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        if(closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send request " + request + ", cause: The channel " + this + " is closed!");
        }
        // create request.
        Request req = new Request();
        req.setVersion("2.0.0");
        req.setTwoWay(true);
        req.setData(request);
        DefaultFuture future = new DefaultFuture(channel, req, timeout);
        try{
            channel.send(req);
        }catch (RemotingException e) {
            future.cancel();
            throw e;
        }
        return future;
    }

    public void close(int timeout) {
        // TODO Auto-generated method stub

    }

    public ExchangeHandler getExchangeHandler() {
        return (ExchangeHandler) channel.getChannelHandler();
    }

}
