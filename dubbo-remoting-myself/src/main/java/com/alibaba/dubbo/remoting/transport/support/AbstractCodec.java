package com.alibaba.dubbo.remoting.transport.support;

import java.io.IOException;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;

public abstract class AbstractCodec implements Codec {

    protected Serialization getSerialization(Channel channel) {
        Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(channel.getUrl().getParameter(Constants.SERIALIZATION_KEY, Constants.DEFAULT_REMOTING_SERIALIZATION));
        return serialization;
    }
    
    
    protected void checkPayload(Channel channel, long size) throws IOException{
        int payload = Constants.DEFAULT_PAYLOAD;
        if(channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getPositiveIntParameter(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
            
            if(size > payload) {
                throw new IOException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel);
            }
        }
    }

}
