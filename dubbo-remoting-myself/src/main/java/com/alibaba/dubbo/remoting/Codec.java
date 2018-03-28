package com.alibaba.dubbo.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;

/**
 * Codec. (SPI,Singleton,ThreadSafe)
 * @author wangyu
 *
 */

@Extension
public interface Codec {
    
    @Adaptive({Constants.CODEC_KEY})
    void encode(Channel channel,OutputStream output,Object message) throws IOException;
    
    @Adaptive({Constants.CODEC_KEY})
    Object decode(Channel channel,InputStream input) throws IOException;
}
