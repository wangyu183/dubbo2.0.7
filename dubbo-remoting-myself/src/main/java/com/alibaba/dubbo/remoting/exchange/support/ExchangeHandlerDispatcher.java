package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.Telnet.support.TelnetHandlerAdapter;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.transport.support.ChannelHandlerDispatcher;

/**
 * ChannelHandlerDispatcher、TelnetHandlerAdapter、ReplierDispatcher的结合体
 * 实现ExchangeHandler（TelnetHandler和ChannelHandler的子接口）
 * ExchangeHandler的方法其中TelnetHandler接口的方法由TelnetHandlerAdapter处理
 * ExchangeHandler的方法其中ChannelHandler接口的方法由ChannelHandlerDispatcher处理
 * ExchangeHandler接口自己的reply(ExchangeChannel channel, Object request)方法由ReplierDispatcher处理
 * @author wangyu
 *
 */
public class ExchangeHandlerDispatcher implements ExchangeHandler {
    
    private final ChannelHandlerDispatcher handlerDispatcher;
    
    private final TelnetHandler telnetHandler;
    
    private final ReplierDispatcher replierDispatcher;
    
    public ExchangeHandlerDispatcher() {
        this.replierDispatcher = new ReplierDispatcher();
        this.handlerDispatcher = new ChannelHandlerDispatcher();
        this.telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(Replier<?> replier){
        replierDispatcher = new ReplierDispatcher(replier);
        handlerDispatcher = new ChannelHandlerDispatcher();
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(ChannelHandler... handlers){
        replierDispatcher = new ReplierDispatcher();
        handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher(Replier<?> replier, ChannelHandler... handlers){
        replierDispatcher = new ReplierDispatcher(replier);
        handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        telnetHandler = new TelnetHandlerAdapter();
    }
    
    public ExchangeHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        handlerDispatcher.addChannelHandler(handler);
        return this;
    }
    
    public ExchangeHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        handlerDispatcher.removeChannelHandler(handler);
        return this;
    }
    
    public<T> ExchangeHandlerDispatcher addReplier(Class<T> type, Replier<T> replier) {
        replierDispatcher.addReplier(type, replier);
        return this;
    }
    
    public <T> ExchangeHandlerDispatcher removeReplier(Class<?> type, Replier<T> replier) {
        replierDispatcher.removeReplier(type);
        return this;
    }
    
    public void connected(Channel channel) throws RemotingException {
        handlerDispatcher.connected(channel);
    }

    public void disconnected(Channel channel) throws RemotingException {
        handlerDispatcher.disconnected(channel);
    }

    public void sent(Channel channel, Object message) throws RemotingException {
        handlerDispatcher.sent(channel, message);
    }

    public void received(Channel channel, Object message) throws RemotingException {
        handlerDispatcher.received(channel, message);
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        handlerDispatcher.caught(channel, exception);
    }

    public String telnet(Channel channel, String message) throws RemotingException {
        return telnetHandler.telnet(channel, message);
    }

    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return ((Replier)replierDispatcher).reply(channel, request);
    }

}
