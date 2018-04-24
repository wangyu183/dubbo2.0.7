package com.alibaba.dubbo.rpc.dubbo;

import java.util.Map;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.AbstractExporter;

public class DubboExporter<T> extends AbstractExporter<T>{
    
    private final String key;
    
    private final Map<String,Exporter<?>> exporterMap;
    
    public DubboExporter(Invoker<T> invoker,String key, Map<String, Exporter<?>> exporterMap) {
        super(invoker);
        this.key = key;
        this.exporterMap = exporterMap;
    }
    
    public void unexport() {
        super.unexport();
        exporterMap.remove(key);
    }

}
