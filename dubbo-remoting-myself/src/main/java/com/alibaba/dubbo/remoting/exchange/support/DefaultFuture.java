package com.alibaba.dubbo.remoting.exchange.support;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

public class DefaultFuture implements ResponseFuture {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFuture.class);
    
    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<Long, Channel>();
    
    private static final Map<Long, DefaultFuture> FUTURES = new java.util.concurrent.ConcurrentHashMap<Long, DefaultFuture>();
    
    private final long id;

    private final Channel channel;

    private final Request request;

    private final int timeout;

    private final Lock lock = new ReentrantLock();

    private final Condition done = lock.newCondition();

    private final long start = System.currentTimeMillis();

    private volatile long sent;

    private volatile Response response;

    private volatile ResponseCallback callback;

    public DefaultFuture(Channel channel, Request request, int timeout) {
        this.channel = channel;
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout > 0 ? timeout : channel.getUrl().getPositiveIntParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        FUTURES.put(id, this);
        CHANNELS.put(id, channel);
    }

    public Object get() throws RemotingException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(int timeoutInMillis) throws RemotingException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCallback(ResponseCallback callback) {
        // TODO Auto-generated method stub

    }

    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }

}
