package com.alibaba.dubbo.rpc.listener;

import java.util.List;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.Invoker;

public class ListenerExporterWrapper<T> implements Exporter<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(ListenerExporterWrapper.class);
    
    private final Exporter<T> exporter;
    
    private final List<ExporterListener> listeners;
    
    public ListenerExporterWrapper(Exporter<T> exporter, List<ExporterListener> listeners) {
        if(exporter == null) {
            throw new IllegalArgumentException("exporter == null");
        }
        this.exporter = exporter;
        this.listeners = listeners;
        if(listeners != null && listeners.size() > 0) {
            RuntimeException exception = null;
            for(ExporterListener listener : listeners) {
                if(listener != null) {
                    try {
                        listener.exported(this);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage(), e);
                        exception = e;
                    }
                }
            }
            if(exception != null) {
                throw exception;
            }
        }
    }
    public Invoker<T> getInvoker() {
        return exporter.getInvoker();
    }

    public void unexport() {
        try {
            
        } finally {
            if(listeners != null && listeners.size() > 0) {
                RuntimeException exception = null;
                for(ExporterListener listener : listeners) {
                    if(listener != null) {
                        try {
                            listener.unexported(this);
                        } catch (RuntimeException e) {
                            logger.error(e.getMessage(), e);
                            exception = e;
                        }
                        
                    }
                }
                if(exception != null) {
                    throw exception;
                }
            }
        }

    }

}
