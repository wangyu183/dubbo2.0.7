package com.alibaba.dubbo.rpc.support;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcConstants;

public abstract class AbstractProtocol implements Protocol {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final Map<String,Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>();
    
    protected final Set<Invoker<?>> invokers = new ConcurrentHashSet<Invoker<?>>();
    
    protected static String serviceKey(URL url) {
        return serviceKey(url.getPort(), url.getPath(), url.getParameter(Constants.DUBBO_VERSION_KEY), url.getParameter(Constants.GROUP_KEY));
    }
    
    protected static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        //StringBuffer is synchronized, StringBuilder is not.
        StringBuilder buf = new StringBuilder();
        if(serviceGroup != null && serviceGroup.length() > 0) {
            buf.append(serviceGroup);
            buf.append("/");
        }
        buf.append(serviceName);
        if(serviceVersion != null && serviceVersion.length() > 0) {
            buf.append(":");
            buf.append(serviceVersion);
        }
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }
    
    public void destory() {
        for(Invoker<?> invoker : invokers) {
            if(invoker != null) {
                invokers.remove(invoker);
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("Destroy reference: " + invoker.getUrl());
                    }
                } catch (Throwable t) {
                    logger.warn(t.getMessage(),t);
                }
            }
        }
        
        for(String key : new ArrayList<String>(exporterMap.keySet())) {
            Exporter<?> exporter = exporterMap.remove(key);
            if(exporter != null) {
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("Unexport service: " + exporter.getInvoker().getUrl());
                    }
                    exporter.unexport();
                } catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        }
    }
    
    
    @SuppressWarnings("deprecation")
    protected static int getServerShutdownTimeout() {
        int timeout = RpcConstants.DEFAULT_SERVER_SHUTDOWN_TIMEOUT;
        String value = System.getProperty(RpcConstants.SHUTDOWN_TIMEOUT_KEY);
        if (value != null && value.length() > 0) {
            try{
                timeout = Integer.parseInt(value);
            }catch (Exception e) {
            }        
        } else {
            value = System.getProperty(RpcConstants.SHUTDOWN_TIMEOUT_SECONDS_KEY);
            if (value != null && value.length() > 0) {
                try{
                    timeout = Integer.parseInt(value) * 1000;
                }catch (Exception e) {
                }        
            }
        }
        
        return timeout;
    }
    

}
