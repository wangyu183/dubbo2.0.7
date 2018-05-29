package com.alibaba.dubbo.common;

/**
 * Node. (API/SPI, Prototype, ThreadSafe)
 * @author wangyu
 *
 */
public interface Node {

    URL getUrl();
    
    boolean isAvailable();
    
    void destroy();
    
}
