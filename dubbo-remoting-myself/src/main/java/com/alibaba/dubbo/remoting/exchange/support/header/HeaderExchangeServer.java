package com.alibaba.dubbo.remoting.exchange.support.header;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;

public class HeaderExchangeServer implements ExchangeServer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1, new NamedThreadFactory("dubbo-remoting-server-heartbeat", true));

    private ScheduledFuture<?> heatbeatTimer;

    private int heartbeat;

    private int heartbeatTimeout;

    private final Server server;

    private volatile boolean closed = false;

    public HeaderExchangeServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.server = server;
        this.heartbeat = server.getUrl().getIntParameter(Constants.HEARTBEAT_KEY, Constants.DEFAULT_HEARTBEAT);
        this.heartbeatTimeout = server.getUrl().getIntParameter(Constants.HEARTBEAT_TIMEOUT_KEY, heartbeat * 3);
        if (heartbeatTimeout < heartbeat * 2) {
            throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
        }
        startHeatbeatTimer();
    }

    private void startHeatbeatTimer() {
        try {
            ScheduledFuture<?> timer = heatbeatTimer;
            if(timer != null && ! timer.isCancelled()) {
                //对于正在执行的线程，如果传入参数为ture,则会选择停止
                timer.cancel(true);
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        if(heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(new HeartBeatTask(), heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }
    
    private boolean isRunning() {
        Collection<Channel> channels = getChannels();
        for(Channel channel : channels) {
            if(DefaultFuture.hasFuture(channel)) {
                return true;
            }
        }
        return false;
    }
    
    public Server getServer() {
        return server;
    }

    public boolean isBound() {
        return server.isBound();
    }

    public Collection<Channel> getChannels() {
        return (Collection) getExchangeChannels();
    }

    public Channel getChannel(InetSocketAddress remoteAddress) {
        return getExchangeChannel(remoteAddress);
    }

    public URL getUrl() {
        return server.getUrl();
    }

    public ChannelHandler getChannelHandler() {
        return server.getChannelHandler();
    }

    public InetSocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    public void send(Object message) throws RemotingException {
        if (closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The server " + getLocalAddress() + " is closed!");
        }
        server.send(message);
    }

    public void send(Object message, boolean sent) throws RemotingException {
        if (closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The server " + getLocalAddress() + " is closed!");
        }
        server.send(message, sent);
    }
    
    private void doClose() {
        if(closed) {
            return;
        }
        closed = true;
        try {
            if(heatbeatTimer != null) {
                heatbeatTimer.cancel(true);
                heatbeatTimer = null;
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            scheduled.shutdown();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    public void close() {
        doClose();
        server.close();
    }

    public void close(int timeout) {
        if(timeout > 0) {
            final long max = (long) timeout;
            final long start = System.currentTimeMillis();
            while(HeaderExchangeServer.this.isRunning() 
                    && System.currentTimeMillis() - start < max) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        doClose();
        server.close();
    }

    public boolean isClosed() {
        return server.isClosed();
    }

    public void reset(URL url) {
        server.reset(url);
        try {
            if (url.hasParameter(Constants.HEARTBEAT_KEY)
                    || url.hasParameter(Constants.HEARTBEAT_TIMEOUT_KEY)) {
                int h = url.getIntParameter(Constants.HEARTBEAT_KEY, heartbeat);
                int t = url.getIntParameter(Constants.HEARTBEAT_TIMEOUT_KEY, h * 3);
                if (t < h * 2) {
                    throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
                }
                if (h != heartbeat || t != heartbeatTimeout) {
                    heartbeat = h;
                    heartbeatTimeout = t;
                    startHeatbeatTimer();
                }
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public Collection<ExchangeChannel> getExchangeChannels() {
        Collection<ExchangeChannel> exchangeChannels = new ArrayList<ExchangeChannel>();
        Collection<Channel> channels = server.getChannels();
        if(channels != null && channels.size() > 0) {
            for(Channel channel : channels) {
                exchangeChannels.add(HeaderExchangeChannel.getOrAddChannel(channel));
            }
        }
        return exchangeChannels;
    }

    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        Channel channel = server.getChannel(remoteAddress);
        return HeaderExchangeChannel.getOrAddChannel(channel);
    }
    
    private class HeartBeatTask implements Runnable{

        public void run() {
            try {
                long now = System.currentTimeMillis();
                for(Channel channel : getChannels()) {
                    try {
                        Long lastRead = (Long) channel.getAttribute(HeaderExchangeHandler.KEY_READ_TIMESTAMP);
                        Long lastWrite = (Long) channel.getAttribute(HeaderExchangeHandler.KEY_WRITE_TIMESTAMP);
                        if((lastRead != null && now - lastRead > heartbeat)
                                ||(lastWrite != null && now - lastWrite > heartbeat)) {
                            Request req = new Request();
                            req.setVersion("2.0.0");
                            req.setTwoWay(true);
                            req.setHeartbeat(true);
                            channel.send(req);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Send heartbeat to client " + channel.getRemoteAddress() + ".");
                            }
                        }
                        if(lastRead != null && now -lastRead > heartbeatTimeout) {
                            logger.warn("Close remote client " + channel.getRemoteAddress()
                            + ", because heartbeat read idle time out.");
                        }
                        channel.close();
                    } catch (Throwable t) {
                        logger.warn("Exception when heartbeat to client " + (InetSocketAddress) channel.getRemoteAddress(), t);
                    }
                }
            } catch (Throwable t) {
                logger.info("Exception when heartbeat to clients: ", t);
            }
        }
        
    }

}
