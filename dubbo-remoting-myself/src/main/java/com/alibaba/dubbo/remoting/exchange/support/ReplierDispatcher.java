package com.alibaba.dubbo.remoting.exchange.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;

/**
 * 类的作用：根据构造函数，传入默认的Replier和map<Class<?>,Replier<?>>，reply方法的作用，即通过map取出request中的Replier或者默认的Replier执行reply方法
 * 类如其名，只是起到一个统计和转发的作用
 * 统计指的是map不同的class 保留新的Replier实例
 * 转发指的是根据class,从map取出Replier，调用Replier实例的reply方法
 * @author wangyu
 *
 */
public class ReplierDispatcher implements Replier<Object> {
    
    private final Replier<?> defaultReplier;
    
    private final Map<Class<?>, Replier<?>> repliers = new ConcurrentHashMap<Class<?>, Replier<?>>();
    
    public ReplierDispatcher() {
        this(null, null);
    }
    
    public ReplierDispatcher(Replier<?> defaultReplier) {
        this(defaultReplier, null);
    }
    
    public ReplierDispatcher(Replier<?> defaultReplier, Map<Class<?>, Replier<?>> repliers) {
        this.defaultReplier = defaultReplier;
        if(repliers != null && repliers.size() > 0) {
            this.repliers.putAll(repliers);
        }
    }
    
    public <T> ReplierDispatcher addReplier(Class<T> type, Replier<T> replier) {
        repliers.put(type, replier);
        return this;
    }
    
    public <T> ReplierDispatcher removeReplier(Class<T> type) {
        repliers.remove(type);
        return this;
    }
    
    private Replier<?> getReplier(Class<?> type){
        for(Map.Entry<Class<?>, Replier<?>> entry : repliers.entrySet()) {
            if(entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }
        if(defaultReplier != null) {
            return defaultReplier;
        }
        
        throw new IllegalStateException("Replier not found, Unsupported message object: " + type );
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return ((Replier)getReplier(request.getClass())).reply(channel, request);
    }

}
