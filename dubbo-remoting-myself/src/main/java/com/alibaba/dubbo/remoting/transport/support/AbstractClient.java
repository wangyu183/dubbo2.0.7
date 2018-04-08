package com.alibaba.dubbo.remoting.transport.support;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.support.handler.WrappedChannelHandler;

public abstract class AbstractClient extends AbstractEndpoint implements Client {
    
protected static final String CLIENT_THREAD_POOL_NAME  ="DubboClientHandler";
    
    private static final AtomicInteger CLIENT_THREAD_POOL_ID = new AtomicInteger();
    
    private final boolean send_reconnect;
    
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;
    
    private final Lock connectLock = new ReentrantLock();
    
    protected volatile ExecutorService executor;
    
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2,new NamedThreadFactory("client-connect-check-timer",true));
    
    public AbstractClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        send_reconnect = url.getBooleanParameter(Constants.SEND_RECONNECT_KEY,false);
        
        try {
            doOpen();
        } catch (Throwable e) {
            close();
            throw new RemotingException(url.getInetSocketAddress(), null, 
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() 
                    + " connect to the server " + getRemoteAddress() + ", cause: " + e.getMessage(), e);

        }
        
        try {
            connect();
            if(logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + getRemoteAddress());
            }
        } catch (RemotingException t) {
            if(url.getBooleanParameter(Constants.CHECK_KEY,true)) {
                close();
                throw t;
            }else {
                logger.error("Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
            }
        }catch (Throwable t){
            close();
            throw new RemotingException(url.getInetSocketAddress(), null, 
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() 
                    + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        if (handler instanceof WrappedChannelHandler ){
            executor = ((WrappedChannelHandler)handler).getExecutor();
        }
        
        
    }

    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        if(channel == null) {
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        }
        return channel.getLocalAddress();
    }

    public void send(Object message, boolean sent) throws RemotingException {
        if(send_reconnect && !isConnected()) {
            connect();
        }
        Channel channel = getChannel();
        if(channel == null || ! channel.isConnected()) {
            throw new RemotingException(this, channel == null ? "channel is null " : (" channel is closed ") +". url:" + getUrl());
        }
        channel.send(message, sent);
    }
    
    private synchronized void initConnectStatusCheckCommand() {
        int reconnect = getReconnectParam(getUrl());
        if(reconnect > 0 && reconnectExecutorFuture == null) {
            Runnable connectStatusCheckCommand = new Runnable() {
                
                public void run() {
                    try {
                        if(!isConnected()) {
                                connect();
                        }
                    } catch (Throwable e) {
                        logger.error("Unexpected error occur at client reconnect",e);
                    }
                }
            };
            reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, reconnect, reconnect, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * 获取到URL类中的reconnect重连间隔
     * @param url
     * @return
     */
    private static int getReconnectParam(URL url) {
        int reconnect;
        String param = url.getParameter(Constants.RECONNECT_KEY);
        if(param == null || param.length() == 0 || "true".equalsIgnoreCase(param)) {
            reconnect = Constants.DEFAULT_RECONNECT_PERIOD;
        }else if("false".equalsIgnoreCase(param)) {
            reconnect = 0;
        }else {
            try{
                reconnect = Integer.parseInt(param);
            }catch (Exception e) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:"+param);
            }
            if(reconnect < 0){
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:"+param);
            }
        }
        return reconnect;
    }
    
    
    private void connect()throws RemotingException {
        connectLock.lock();
        
        try {
            if(isConnected()) {
                return;
            }
            initConnectStatusCheckCommand();
            doConnect();
            if (! isConnected()) {
                throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName()
                                            + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion()
                                            + ", cause: Connect wait timeout: " + getTimeout() + "ms.");
            }
        } catch (Throwable e) {
            throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName()
                    + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion()
                    + ", cause: " + e.getMessage(), e);
        }finally {
            connectLock.unlock();
        }
        
    }
    
    public void disconnect() {
        connectLock.lock();
        try {
            destroyConnectStatusCheckCommand();
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }
    
    /**
     * 取消正在执行的任务并从ScheduledThreadPoolExecutor中删除
     */
    private synchronized void destroyConnectStatusCheckCommand(){
        try {
            if (reconnectExecutorFuture != null && ! reconnectExecutorFuture.isDone()){
                reconnectExecutorFuture.cancel(true);
                //尝试从工作队列中删除已取消的所有Future任务
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }    
    
    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool(new NamedThreadFactory(CLIENT_THREAD_POOL_NAME + CLIENT_THREAD_POOL_ID.incrementAndGet() + "-" + getUrl().getAddress(), true));
    }
    
    
    public void close() {
        ExecutorUtil.shutdownNow(executor, 100);
        super.close();
        disconnect();
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    public void close(int timeout) {
        ExecutorUtil.gracefulShutdown(executor ,timeout);
        close();
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " [" + getLocalAddress() + " -> " + getRemoteAddress() + "]";
    }
    
    // -----------------------channel-------------------------
    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        if(channel == null) {
            return getUrl().getInetSocketAddress();
        }
        return channel.getRemoteAddress();
    }

    public boolean isConnected() {
        Channel channel = getChannel();
        if(channel == null) {
            return false;
        }
        return channel.isConnected();
    }

    public boolean hasAttribute(String key) {
        Channel channel = getChannel();
        if(channel == null) {
            return false;
        }
        return channel.hasAttribute(key);
    }

    public Object getAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null)
            return null;
        return channel.getAttribute(key);
    }

    public void setAttribute(String key, Object value) {
        Channel channel = getChannel();
        if (channel == null)
            return;
        channel.setAttribute(key, value);

    }

    public void removeAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null)
            return;
        channel.removeAttribute(key);

    }
 /**************************************************************/   
    
    /**
     * Client interface.
     */
    public void reconnect() throws RemotingException {
        disconnect();
        connect();
    }
    
    /**
     * Open client.
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;
    
    
    /**
     * Close client.
     * @throws Throwable
     */
    protected abstract void doClose() throws Throwable;
    
    
    /**
     * Connect to server.
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * disConnect to server.
     * @throws Throwable
     */
    protected abstract void doDisConnect() throws Throwable;


    /**
     * Get the connected channel.
     * @return
     */
    protected abstract Channel getChannel();
}
