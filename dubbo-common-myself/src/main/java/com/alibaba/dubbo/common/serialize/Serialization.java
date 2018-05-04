package com.alibaba.dubbo.common.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;

/**
 * SPI Singleton ThreadSafe
 * @author wangyu
 *
 */

@Extension("dubbo")
public interface Serialization {
    
    byte getContentTypeId();
    
    String getContentType();
    
    @Adaptive
    ObjectOutput serialize(URL url, OutputStream output) throws IOException;
    
    @Adaptive
    ObjectInput deserialize(URL url, InputStream input) throws IOException;
    
}
