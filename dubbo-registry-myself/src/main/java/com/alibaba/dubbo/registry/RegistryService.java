package com.alibaba.dubbo.registry;

import java.util.List;

import com.alibaba.dubbo.common.URL;

public interface RegistryService {
    
    void register(URL url);
    
    void unregister(URL url);
    
    void subscribe(URL url, NotifyListener listener);
    
    void unsubscribe(URL url, NotifyListener listener);
    
    List<URL> lookup(URL url);
}
