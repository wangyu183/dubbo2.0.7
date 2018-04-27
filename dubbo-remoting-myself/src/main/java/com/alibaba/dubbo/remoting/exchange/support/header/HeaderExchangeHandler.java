package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ExecutionException;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;

public class HeaderExchangeHandler implements ChannelHandler {
    
    protected static final Logger logger = LoggerFactory.getLogger(HeaderExchangeHandler.class);
    
    public static String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";
    
    public static String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";
    
    private final ExchangeHandler handler;
    
    public HeaderExchangeHandler(ExchangeHandler handler) {
        if(handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
    }
    
    
    public void connected(Channel channel) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.connected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    public void disconnected(Channel channel) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.disconnected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    public void sent(Channel channel, Object message) throws RemotingException {
        Throwable exception = null;
        try {
            channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
            ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
            try {
                handler.sent(exchangeChannel, message);
            } finally {
                HeaderExchangeChannel.removeChannelIfDisconnected(channel);
            }
        } catch (Throwable t) {
            exception = t;
        }
        if(message instanceof Request) {
            Request request = (Request)message;
            DefaultFuture.sent(channel, request);
        }
        
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else if (exception instanceof RemotingException) {
                throw (RemotingException) exception;
            } else {
                throw new RemotingException(channel.getLocalAddress(), channel.getRemoteAddress(),
                                            exception.getMessage(), exception);
            }
        }
    }

    public void received(Channel channel, Object message) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            if(message instanceof Request) {
                Request request = (Request) message;
                if(request.isTwoWay()) {
                    Response response = handleRequest(exchangeChannel, request);
                    if (response == null) {
                        throw new RemotingException(channel, "Response is null.");
                    }
                    channel.send(response);
                }else {
                    handler.received(exchangeChannel, channel);
                }
            }else if(message instanceof Response) {
                handleResponse(channel,(Response)message);
            }else if(message instanceof String) {
                String echo = handler.telnet(channel, (String)message);
                if(echo != null && echo.length() > 0) {
                    channel.send(echo);
                }
            }else {
                handler.received(exchangeChannel, message);
            }
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        if (exception instanceof ExecutionException) {
            ExecutionException e = (ExecutionException) exception;
            Object msg = e.getRequest();
            if (msg instanceof Request) {
                Request req = (Request) msg;
                if (req.isTwoWay() && ! req.isHeartbeat()) {
                    Response res = new Response(req.getId(), req.getVersion());
                    res.setStatus(Response.SERVER_ERROR);
                    res.setErrorMessage(StringUtils.toString(e));
                    channel.send(res);
                    return;
                }
            }
        }
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.caught(exchangeChannel, exception);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }
    
    Response handleRequest(ExchangeChannel channel, Request req) throws RemotingException{
        Response res = new Response(req.getId(),req.getVersion());
        if(req.isHeartbeat()) {
            res.setHeartbeat(true);
            return res;
        }
        if(req.isBroken()) {
            Object data = req.getData();
            String msg;
            if(data == null) {
                msg = null;
            }else if(data instanceof Throwable) {
                msg = StringUtils.toString((Throwable)data);
            }else {
                msg = data.toString();
            }
            res.setErrorMessage("Fail to decode request due to: " + msg);
            res.setStatus(Response.BAD_REQUEST);
            return res;
        }
        
        Object msg = req.getData();
        if(handler == null) {
            res.setStatus(Response.SERVICE_NOT_FOUND);
            res.setErrorMessage("InvokeHandler not found, Unsupported protocol object: " + msg);
        }else {
            try {
                Object result = handler.reply(channel, msg);
                res.setStatus(Response.OK);
                res.setResult(result);
            } catch (Throwable e) {
                res.setStatus(Response.SERVER_ERROR);
                res.setErrorMessage(StringUtils.toString(e));
            }
        }
        return res;
    }
    
    static void handleResponse(Channel channel, Response response) throws RemotingException{
        if(response != null && !response.isHeartbeat()) {
            DefaultFuture.received(channel, response);
        }
    }

}