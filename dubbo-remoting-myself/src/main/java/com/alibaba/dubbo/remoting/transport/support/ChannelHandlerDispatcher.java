package com.alibaba.dubbo.remoting.transport.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;

public class ChannelHandlerDispatcher implements ChannelHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ChannelHandlerDispatcher.class);
    
    private final Collection<ChannelHandler> channelHandlers = new CopyOnWriteArraySet<ChannelHandler>();
    
    public ChannelHandlerDispatcher() {}
    
    public ChannelHandlerDispatcher(ChannelHandler... handlers) {
        this(handlers == null ? null : Arrays.asList(handlers));
    }
    
    public ChannelHandlerDispatcher(Collection<ChannelHandler> handlers) {
        if(handlers != null && handlers.size() > 0) {
            this.channelHandlers.addAll(handlers);
        }
    }
    

    public Collection<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    public ChannelHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        this.channelHandlers.add(handler);
        return this;
    }

    public ChannelHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        this.channelHandlers.remove(handler);
        return this;
    }
    
    public void connected(Channel channel) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.connected(channel);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void disconnected(Channel channel) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.disconnected(channel);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void sent(Channel channel, Object message) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.sent(channel, message);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void received(Channel channel, Object message) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.received(channel, message);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.caught(channel, exception);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

}
