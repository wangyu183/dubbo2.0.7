package com.alibaba.dubbo.remoting.exchange.support.header;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

public class HeaderExchangeClient implements ExchangeClient {
    
    private final Client client;
    
    private final ExchangeChannel channel;
    
    public HeaderExchangeClient(Client client) {
        if(client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.client = client;
        //client extends channel
        this.channel = new HeaderExchangeChannel(client);
    }
    
    
    /**
     * Client.
     */
    public void reconnect() throws RemotingException {
        client.reconnect();
    }
    
    /**
     * Endpoint. start
     */
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
        channel.send(message);
    }

    public void send(Object message, boolean sent) throws RemotingException {
        channel.send(message, sent);
    }

    public void close() {
        channel.close();
    }

    public void close(int timeout) {
        channel.close();
    }
    
    /**
     * EndPoint. end
     */
    public boolean isClosed() {
        return channel.isClosed();
    }
    
    /**
     * Channel.start
     */
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
    
    /**
     * Channel.end
     */
    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }
    
   
    /**
     * Resetable
     */
    public void reset(URL url) {
        client.reset(url);
    }
    
    /**
     * ExchangeChannel
     */
    public ResponseFuture request(Object request) throws RemotingException {
        return channel.request(request);
    }

    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return channel.request(request, timeout);
    }

    public ExchangeHandler getExchangeHandler() {
        return channel.getExchangeHandler();
    }

}
