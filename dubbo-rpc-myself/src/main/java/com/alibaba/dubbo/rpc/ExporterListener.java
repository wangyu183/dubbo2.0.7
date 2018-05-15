package com.alibaba.dubbo.rpc;

public interface ExporterListener {
    
    void exported(Exporter<?> exporter) throws RpcException;
    
    void unexported(Exporter<?> exporter);
}
