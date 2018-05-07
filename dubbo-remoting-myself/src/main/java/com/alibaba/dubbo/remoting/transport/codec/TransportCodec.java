package com.alibaba.dubbo.remoting.transport.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.transport.support.AbstractCodec;

@Extension("transport")
public class TransportCodec extends AbstractCodec {

    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
        ObjectOutput objectOutput = getSerialization(channel).serialize(channel.getUrl(), output);
        encodeData(channel, objectOutput, message);
        objectOutput.flushBuffer();
    }

    public Object decode(Channel channel, InputStream input) throws IOException {
        return decodeData(channel, getSerialization(channel).deserialize(channel.getUrl(), input));
    }
    
    protected void encodeData(ObjectOutput output, Object message) throws IOException {
        output.writeObject(message);
    }
    
    protected Object decodeData(ObjectInput input) throws IOException{
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }
    
    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return decodeData(input);
    }
    
    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
        encodeData(output, message);
    }

}
