package com.alibaba.dubbo.rpc.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;

public abstract class AbstractInvoker<T> implements Invoker<T> {
    protected final Logger   logger    = LoggerFactory.getLogger(getClass());
    
    private final Class<T> type;
    
    private final URL url;
    
    private final Map<String, String> attachment;
    
    private volatile boolean available = true;
    
    private volatile boolean destroyed = false;
    
    
    public AbstractInvoker(Class<T> type, URL url) {
        this(type, url , (Map<String, String>)null);
    }
    
    public AbstractInvoker(Class<T> type, URL url, String[] keys) {
        this(type, url, convertAttachment(url, keys));
    }
    
    public AbstractInvoker(Class<T> type, URL url, Map<String, String> attachment) {
        if(type == null) {
            throw new IllegalArgumentException("service type == null");
        }
        if(url == null) {
            throw new IllegalArgumentException("service url == null");
        }
        this.type = type;
        this.url = url;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }
    
    private static Map<String, String> convertAttachment(URL url, String[] keys ){
        if(keys == null || keys.length == 0) {
            return null;
        }
        Map<String, String> attachment = new HashMap<String, String>();
        for(String key : keys) {
            String value = url.getParameter(key);
            if(value != null && value.length() > 0) {
                attachment.put(key, value);
            }
        }
        return attachment;
    }
    
    public Class<T> getInterface(){
        return type;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    protected void setAvailable(boolean available) {
        this.available = available;
    }
    
    public void destroy() {
        if(destroyed) {
            throw new IllegalStateException("The invoker " + this + "destroyed!");
        }
        destroyed = true;
        setAvailable(false);
    }
    
    @Override
    public String toString() {
        return getInterface() + " -> " + getUrl() == null ? " " : getUrl().toString();
    }
    
    /**
     * 将RpcInvocation 和 RpcContext 中保存的Map 保存到RpcInvocation中的 Map<> attachments中
     * 调用抽象doInvoke(invocation)方法
     */
    public Result invoke(Invocation inv) throws RpcException{
        if(destroyed) {
            throw new RpcException("Rpc invoker for service " + this + " on consumer " + NetUtils.getLocalHost() 
            + " use dubbo version " + Version.getVersion()
            + " is DESTROYED, can not be invoked any more!");
        }
        RpcInvocation invocation = (RpcInvocation) inv;
        Map<String, String> attachments = new HashMap<String, String>();
        if(attachment != null && attachment.size() > 0) {
            attachments.putAll(attachment);
        }
        Map<String, String> context = RpcContext.getContext().getAttachments();
        if (context != null) {
            attachments.putAll(context);
        }
        if (invocation.getAttachments() != null) {
            attachments.putAll(invocation.getAttachments());
        }
        invocation.setAttachments(attachments);
        RpcResult result = new RpcResult();
        try {
            Object obj = doInvoke(invocation);
            result.setResult(obj);
        }catch (InvocationTargetException e) { // biz exception
            Throwable te = e.getTargetException();
            if (te == null) {
                result.setException(e);
            } else {
                if (te instanceof RpcException) {
                    ((RpcException) te).setCode(RpcException.BIZ_EXCEPTION);
                }
                result.setException(te);
            }
        } catch (RpcException e) {
            if (e.isBiz()) {
                result.setException(e);
            } else {
                throw e;
            }
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }
    
    protected abstract Object doInvoke(Invocation invocation) throws Throwable;
}
