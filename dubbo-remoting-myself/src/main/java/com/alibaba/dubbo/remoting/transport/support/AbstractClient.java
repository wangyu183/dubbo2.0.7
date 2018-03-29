package com.alibaba.dubbo.remoting.transport.support;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;

public abstract class AbstractClient extends AbstractEndpoint implements Client {
    
    private final boolean send_reconnect;
    
    public AbstractClient(URL url, ChannelHandler handler) {
        super(url, handler);
        // TODO Auto-generated constructor stub
        send_reconnect = url.getBooleanParameter(Constants.SEND_RECONNECT_KEY,false);
        
        try {
            doOpen();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            close();
        }
        
    }

    public InetSocketAddress getLocalAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    public void send(Object message, boolean sent) throws RemotingException {
        // TODO Auto-generated method stub

    }
    
    public void close() {
    }
    
    
    
    
    
    
    // -----------------------channel-------------------------
    public InetSocketAddress getRemoteAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasAttribute(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getAttribute(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttribute(String key, Object value) {
        // TODO Auto-generated method stub

    }

    public void removeAttribute(String key) {
        // TODO Auto-generated method stub

    }

    public void reconnect() throws RemotingException {
        // TODO Auto-generated method stub

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
