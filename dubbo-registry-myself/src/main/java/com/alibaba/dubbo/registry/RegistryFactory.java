package com.alibaba.dubbo.registry;


import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;

@Extension("dubbo")
public interface RegistryFactory {
    
    Registry getRegistry(URL url);
    
}
