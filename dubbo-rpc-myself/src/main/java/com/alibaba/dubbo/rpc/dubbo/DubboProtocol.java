package com.alibaba.dubbo.rpc.dubbo;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.exchange.support.ExchangeHandlerAdapter;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcConstants;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.support.AbstractProtocol;

public class DubboProtocol extends AbstractProtocol{
    
    public static final String NAME = "dubbo";
    
    public static final String COMPATIBLE_CODEC_NAME = "dubbo1compatible";
    
    public static final int DEFAULT_PORT = 20880;
    
 // <host:port,Exchanger>
    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<String,ExchangeServer>();
    
    private final ConcurrentHashMap<String,String> stubServiceMethodsMap = new ConcurrentHashMap<String,String>();
    
    private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
    
        private boolean isClientSide(Channel channel) {
            InetSocketAddress address = channel.getRemoteAddress();
            URL url = channel.getUrl();
            return url.getPort() == address.getPort() &&
                    NetUtils.filterLocalHost(channel.getUrl().getHost())
                    .equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
        }
        
        @Override
        public Object reply(ExchangeChannel channel, Object message) throws RemotingException{
           if(message instanceof Invocation) {
               boolean isCallBackServiceInvoke = false;
               boolean isStubServiceInvoke = false;
               Invocation inv = (Invocation) message;
               int port = channel.getLocalAddress().getPort();
               String path = inv.getAttachments().get(Constants.PATH_KEY);
               isStubServiceInvoke = Boolean.TRUE.toString().equals(inv.getAttachments().get(RpcConstants.STUB_EVENT_KEY));
               if(isStubServiceInvoke) {
                   port = channel.getRemoteAddress().getPort();
               }
               isCallBackServiceInvoke = isClientSide(channel) && !isStubServiceInvoke;
               if(isCallBackServiceInvoke){
                   path = inv.getAttachments().get(Constants.PATH_KEY)+"."+inv.getAttachments().get(RpcConstants.CALLBACK_SERVICE_KEY);
               }
               String serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.VERSION_KEY), inv.getAttachments().get(Constants.GROUP_KEY));
               DubboExporter<?> exporter = (DubboExporter<?>) exporterMap.get(serviceKey);
               if (exporter == null) {
                   throw new RemotingException(channel, "Not found exported service: " + serviceKey + " in " + exporterMap.keySet() + ", may be version or group mismatch " + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress() + ", message:"+message);
               }
               if (isCallBackServiceInvoke){
                   String methodsStr = exporter.getInvoker().getUrl().getParameters().get("methods");
                   boolean hasMethod = false;
                   if (methodsStr == null || methodsStr.indexOf(",") == -1){
                       hasMethod = inv.getMethodName().equals(methodsStr);
                   } else {
                       String[] methods = methodsStr.split(",");
                       for (String method : methods){
                           if (inv.getMethodName().equals(method)){
                               hasMethod = true;
                               break;
                           }
                       }
                   }
                   if (!hasMethod){
                       logger.warn(new IllegalStateException("The methodName "+inv.getMethodName()+" not found in callback service interface ,invoke will be ignored. please update the api interface. url is:" + exporter.getInvoker().getUrl()) +" ,invocation is :"+inv );
                       return null;
                   }
               }
               return exporter.invoke(inv, channel.getRemoteAddress());
           }
           throw new RemotingException(channel, "Unsupported request: " + message == null ? null : (message.getClass().getName() + ": " + message) + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }
        
        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                reply((ExchangeChannel) channel, message);
            } else {
                super.received(channel, message);
            }
        }
        
        @Override
        public void connected(Channel channel) throws RemotingException{
            
        }
        
        
        
        
        
        
        private void invoke(Channel channel, String methodKey) {
            
        }
        
        private Invocation createInvocation(Channel channel, URL url, String methodKey) {
            String method = url.getParameter(methodKey);
            if (method == null || method.length() == 0) {
                return null;
            }
        }
    };
    
    
    
    
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
