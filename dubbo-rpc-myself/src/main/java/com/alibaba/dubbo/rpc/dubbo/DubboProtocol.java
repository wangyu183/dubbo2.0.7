package com.alibaba.dubbo.rpc.dubbo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.support.AbstractProtocol;

public class DubboProtocol extends AbstractProtocol{
    
    public static final String NAME = "dubbo";
    
    public static final String COMPATIBLE_CODEC_NAME = "dubbo1compatible";
    
    public static final int DEFAULT_PORT = 20880;
    
 // <host:port,Exchanger>
    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<String,ExchangeServer>();
    
    private final ConcurrentHashMap<String,String> stubServiceMethodsMap = new ConcurrentHashMap<String,String>();
    
    private
    
    
    
    
    private static DubboProtocol INSTANCE;
    
    public DubboProtocol() {
        INSTANCE = this;
    }
    
    public static DubboProtocol getDubboProtocol() {
        if(INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(DubboProtocol.NAME);
        }
        return INSTANCE;
    }
    
    public int getDefaultPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        // TODO Auto-generated method stub
        return null;
    }

}
