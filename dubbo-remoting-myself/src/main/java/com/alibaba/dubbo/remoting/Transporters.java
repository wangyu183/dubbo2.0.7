package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.remoting.transport.support.ChannelHandlerAdapter;
import com.alibaba.dubbo.remoting.transport.support.ChannelHandlerDispatcher;

public class Transporters {

    private Transporters() {}
    
    static {
        // check duplicate jar package
        Version.checkDuplicate(Transporters.class);
        Version.checkDuplicate(RemotingException.class);
    }
    
    public static Transporter getTransporter() {
        return ExtensionLoader.getExtensionLoader(Transporter.class).getAdaptiveExtension();
    }
    
    public static Client connect(String url, ChannelHandler... handler) throws RemotingException {
        return connect(URL.valueOf(url), handler);
    }
    
    public static Client connect(URL url, ChannelHandler...  handlers) throws RemotingException{
        if(url == null) {
            throw new IllegalArgumentException("url == null");
        }
        ChannelHandler handler;
        if(handlers == null || handlers.length == 0) {
            handler = new ChannelHandlerAdapter();
        }else if(handlers.length == 1) {
            handler = handlers[0];
        }else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return getTransporter().connect(url, handler);
    }
    
    public static Server bind(String url, ChannelHandler... handler) throws RemotingException {
        return bind(URL.valueOf(url), handler);
    }

    public static Server bind(URL url, ChannelHandler... handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handlers == null || handlers.length == 0) {
            throw new IllegalArgumentException("handlers == null");
        }
        ChannelHandler handler;
        if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return getTransporter().bind(url, handler);
    }
    
}
