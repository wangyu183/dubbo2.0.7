package com.alibaba.dubbo.rpc.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcConstants;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.listener.ListenerExporterWrapper;
import com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper;

public class ProtocolListenerWrapper implements Protocol {
    
    private final Protocol protocol;
    
    public ProtocolListenerWrapper(Protocol protocol) {
        if(protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if(Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            return protocol.export(invoker);
        }
        return new ListenerExporterWrapper<T>(protocol.export(invoker), 
                buildServiceListeners(invoker.getUrl().getParameter(Constants.EXPORTER_LISTENER_KEY), RpcConstants.DEFAULT_EXPORTER_LISTENERS));
    }

    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            return protocol.refer(type, url);
        }
        return new ListenerInvokerWrapper<T>(protocol.refer(type, url), 
                buildReferenceListeners(url.getParameter(Constants.INVOKER_LISTENER_KEY), RpcConstants.DEFAULT_INVOKER_LISTENERS));
    }

    public void destory() {
        protocol.destory();

    }

    private static List<ExporterListener> buildServiceListeners(String config, List<String> defaults){
        List<String> names = ConfigUtils.mergeValues(Filter.class, config, defaults);
        List<ExporterListener> listeners = new ArrayList<ExporterListener>();
        if(names.size() > 0) {
            for(String name : names) {
                listeners.add(ExtensionLoader.getExtensionLoader(ExporterListener.class).getExtension(name));
            }
        }
        return Collections.unmodifiableList(listeners);
    }
    
    private static List<InvokerListener> buildReferenceListeners(String config, List<String> defaults) {
        List<String> names = ConfigUtils.mergeValues(Filter.class, config, defaults);
        List<InvokerListener> listeners = new ArrayList<InvokerListener>();
        if (names.size() > 0) {
            for (String name : names) {
                listeners.add(ExtensionLoader.getExtensionLoader(InvokerListener.class).getExtension(name));
            }
        }
        return Collections.unmodifiableList(listeners);
    }
}
