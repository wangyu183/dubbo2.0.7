package com.alibaba.dubbo.remoting.transport.support;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.support.handler.WrappedChannelHandler;

public abstract class AbstractServer extends AbstractEndpoint implements Server {
    
    private InetSocketAddress localAddress;
    
    private InetSocketAddress bindAddress;
    
    private int accepts;
    
    private int idleTimeout = 600;
    
    protected static final String SERVER_THREAD_POOL_NAME = "DubboServerHandler";
    
    ExecutorService executor;
    
    public AbstractServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        // TODO Auto-generated constructor stub
        localAddress = getUrl().getInetSocketAddress();
        String host = url.getBooleanParameter(Constants.ANYHOST_KEY)
                ||  NetUtils.isInvalidLocalHost(getUrl().getHost())
                ? NetUtils.ANYHOST : getUrl().getHost();
        bindAddress = new InetSocketAddress(host, getUrl().getPort());
        this.accepts = url.getIntParameter(Constants.ACCEPTS_KEY, Constants.DEFAULT_ACCEPTS);
        this.idleTimeout = url.getIntParameter(Constants.IDLE_TIMEOUT_KEY, Constants.DEFAULT_IDLE_TIMEOUT);
        
        try {
            doOpen();
        } catch (Throwable t) {
            throw new RemotingException(url.getInetSocketAddress(), null, "Failed to bind " + getClass().getSimpleName() 
                    + " on " + getLocalAddress() + ", cause: " + t.getMessage(), t);
        }
        
        if(handler instanceof WrappedChannelHandler) {
            executor = ((WrappedChannelHandler)handler).getExecutor();
        }
    }
    
    protected abstract void doOpen() throws Throwable;
    
    protected abstract void doClose() throws Throwable;
    
    public void close(int timeout) {
        ExecutorUtil.gracefulShutdown(executor ,timeout);
        close();
    }
    
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public int getAccepts() {
        return accepts;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void send(Object message, boolean sent) throws RemotingException {
        Collection<Channel> channels = getChannels();
        for(Channel channel : channels) {
            channel.send(message,sent);
        }
    }
    
    public void close() {
        ExecutorUtil.shutdownNow(executor ,100);
        try {
            super.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    
    @Override
    public void connected(Channel ch) throws RemotingException {
        Collection<Channel> channels = getChannels();
        if (accepts > 0 && channels.size() > accepts) {
            logger.error("Close channel " + ch + ", cause: The server " + ch.getLocalAddress() + " connections greater than max config " + accepts);
            ch.close();
            return;
        }
        super.connected(ch);
    }
    
    @Override
    public void disconnected(Channel ch) throws RemotingException {
        Collection<Channel> channels = getChannels();
        if (channels.size() == 0){
            logger.warn("All clients has discontected from " + ch.getLocalAddress() + ". You can graceful shutdown now.");
        }
        super.disconnected(ch);
    }
    
    protected Codec getDownstreamCodec() {
        Codec downstreamCodec = getCodec();
        String downstreamCodecStr = getUrl().getParameter(Constants.DOWNSTREAM_CODEC_KEY);
        if(downstreamCodecStr != null ){
            downstreamCodec = ExtensionLoader.getExtensionLoader(Codec.class).getExtension(downstreamCodecStr);
        }
        return downstreamCodec;
    }
}
