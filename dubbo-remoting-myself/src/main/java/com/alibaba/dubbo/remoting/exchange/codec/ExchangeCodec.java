package com.alibaba.dubbo.remoting.exchange.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.io.StreamUtils;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Telnet.codec.TelnetCodec;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;

@Extension("exchange")
public class ExchangeCodec extends TelnetCodec {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeCodec.class);
    
    protected static final int HEADER_LENGTH = 16;
    
    protected static final short MAGIC = (short) 0xdabb;
    
    protected static final byte MAGIC_HIGH = (byte) Bytes.short2bytes(MAGIC)[0];
    
    protected static final byte MAGIC_LOW = (byte) Bytes.short2bytes(MAGIC)[1];
    
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    
    protected static final byte     FLAG_TWOWAY        = (byte) 0x40;

    protected static final byte     FLAG_HEARTBEAT     = (byte) 0x20;
    
    protected static final int SERIALIZATION_MASK = 0x1f;
    
    private static Map<Byte, Serialization> ID_SERIALIZATION_MAP = new HashMap<Byte, Serialization>();
    
    static {
        Set<String> supportedExtensions = ExtensionLoader.getExtensionLoader(Serialization.class).getSupportedExtensions();
        for(String name : supportedExtensions) {
            Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(name);
            byte idByte = serialization.getContentTypeId();
            if (ID_SERIALIZATION_MAP.containsKey(idByte)) {
                logger.error("Serialization extension " + serialization.getClass().getName()
                             + " has duplicate id to Serialization extension "
                             + ID_SERIALIZATION_MAP.get(idByte).getClass().getName()
                             + ", ignore this Serialization extension");
                continue;
            }
            ID_SERIALIZATION_MAP.put(idByte, serialization);
        }
    }
    
    public void encode(Channel channel, OutputStream os, Object msg) throws IOException {
        if(msg instanceof Request) {
            encodeRequest(channel, os, (Request) msg);
        }else if(msg instanceof Response) {
            encodeResponse(channel, os, (Response) msg);
        }else {
            super.encode(channel, os, msg);
        }
    }
    
    protected void encodeRequest(Channel channel, OutputStream os, Request request) throws IOException {
        Serialization serialization = getSerialization(channel);
        byte[] header = new byte[HEADER_LENGTH];
        Bytes.short2bytes(MAGIC, header);
        
        header[2] = (byte) (FLAG_REQUEST | serialization.getContentTypeId());
    
        if(request.isTwoWay()) {
            header[2] |= FLAG_TWOWAY;
        }
        
        if(request.isHeartbeat()) {
            header[2] |= FLAG_HEARTBEAT;
        }
        
        Bytes.long2bytes(request.getId(), header, 4);
    
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
        ObjectOutput out = serialization.serialize(channel.getUrl(), bos);
        if(request.isHeartbeat()) {
            encodeHeartbeatData(channel, out, request.getData());
        }else {
            encodeRequestData(channel, out, request.getData());
        }
        out.flushBuffer();
        bos.flush();
        bos.close();
        byte[] data = bos.toByteArray();
        Bytes.int2bytes(data.length, header, 12);
        os.write(header);
        os.write(data);
    }
    
    protected void encodeResponse(Channel channel, OutputStream os, Response response) throws IOException {
        Serialization serialization = getSerialization(channel);
        byte[] header = new byte[HEADER_LENGTH];
        Bytes.short2bytes(MAGIC, header);
        header[2] = serialization.getContentTypeId();
        if(response.isHeartbeat()) {
            header[2] |= FLAG_HEARTBEAT;
        }
        byte status = response.getStatus();
        header[3] = status;
        Bytes.long2bytes(response.getId(), header, 4);
        
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(1024);
        ObjectOutput out = serialization.serialize(channel.getUrl(), bos);
        if(status == Response.OK) {
            if(response.isHeartbeat()) {
                encodeHeartbeatData(channel, out, response.getResult());
            }else {
                encodeResponseData(channel, out, response.getResult());
            }
            
        }else {
            out.writeUTF(response.getErrorMessage());
        }
        out.flushBuffer();
        bos.flush();
        bos.close();
        byte[] data = bos.toByteArray();
        Bytes.int2bytes(data.length, header, 12);
        os.write(header);
        os.write(data);
    }
    
    protected void encodeHeartbeatData(Channel channel, ObjectOutput out, Object data) throws IOException{
        encodeHeartbeatData(out, data);
    }
    
    protected void encodeHeartbeatData(ObjectOutput out, Object data) throws IOException {
        out.writeObject(data);
    }
    
    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        encodeRequestData(out, data);
    }
    
    protected void encodeRequestData(ObjectOutput out, Object data) throws IOException {
        out.writeObject(data);
    }
    
    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException{
        encodeResponseData(out, data);
    }
    
    protected void encodeResponseData(ObjectOutput out, Object data) throws IOException{
        out.writeObject(data);
    }
    
    public Object decode(Channel channel, InputStream is) throws IOException{
        int readable = is.available();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        is.read(header);
        return decode(channel, is, readable, header);
    }
    
    protected Object decode(Channel channel, InputStream is, int readable, byte[] header) throws IOException {
        if(readable > 0 && header[0] != MAGIC_HIGH
                || readable > 1 && header[1] != MAGIC_LOW) {
            int length = header.length;
            if(header.length < readable) {
                header = Bytes.copyOf(header, readable);
                is.read(header, length, readable - length);
            }
            for(int i = 1; i < header.length - 1; i++) {
                if(header[i] == MAGIC_HIGH && header[i + 1] == MAGIC_LOW) {
                    UnsafeByteArrayInputStream bis = ((UnsafeByteArrayInputStream) is);
                    bis.position(bis.position() - header.length + i);
                    header = Bytes.copyOf(header, i);
                    break;
                }
            }
            return super.decode(channel, is, readable, header);
        }
        if(readable < HEADER_LENGTH) {
            return NEED_MORE_INPUT;
        }
        
        int len = Bytes.bytes2int(header, 12);
        checkPayload(channel, len);
        
        int tt = len + HEADER_LENGTH;
        if(readable < tt) {
            return NEED_MORE_INPUT;
        }
        
        if(readable != tt) {
            is = StreamUtils.limitedInputStream(is, len);
        }
        
        byte flag = header[2]; 
        byte proto = (byte)(flag &  SERIALIZATION_MASK);
        Serialization s = getSerializationById(proto);
        if (s == null) {
            s = getSerialization(channel);
        }
        ObjectInput in = s.deserialize(channel.getUrl(), is);
        long id = Bytes.bytes2long(header, 4);
        if((flag & FLAG_REQUEST) == 0) {
            Response res = new Response(id);
            res.setHeartbeat(( flag & FLAG_HEARTBEAT ) != 0);
            byte status = header[3];
            res.setStatus(status);
            if( status == Response.OK ) {
                try {
                    Object data;
                    if (res.isHeartbeat()) {
                        data = decodeHeartbeatData(channel, in);
                    } else {
                        data = decodeResponseData(channel, in);
                    }
                    res.setResult(data);
                } catch (Throwable t) {
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(in.readUTF());
            }
            return res;
        }else {
            Request req = new Request(id);
            req.setVersion("2.0.0");
            req.setTwoWay( ( flag & FLAG_TWOWAY ) != 0 );
            req.setHeartbeat( ( flag & FLAG_HEARTBEAT ) != 0 );
            try {
                Object data;
                if (req.isHeartbeat()) {
                    data = decodeHeartbeatData(channel, in);
                } else {
                    data = decodeRequestData(channel, in);
                }
                req.setData(data);
            } catch (Throwable t) {
                // bad request
                req.setBroken(true);
                req.setData(t);
            }
            return req; 
        }
    }
    
    private static final Serialization getSerializationById(Byte id) {
        return ID_SERIALIZATION_MAP.get(id);
    }
    
    @Override
    protected Object decodeData(ObjectInput in) throws IOException {
        return decodeRequestData(in);
    }
    
    @Override
    protected Object decodeData(Channel channel, ObjectInput in) throws IOException {
        return decodeRequestData(channel ,in);
    }
    
    protected Object decodeHeartbeatData(Channel channel, ObjectInput in) throws IOException{
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }
    
    protected Object decodeResponseData(Channel channel, ObjectInput in) throws IOException {
        return decodeRequestData(in);
    }
    
    protected Object decodeRequestData(Channel channel, ObjectInput in) throws IOException {
        return decodeRequestData(in);
    }
    
    protected Object decodeRequestData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeResponseData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }
    
    public static void main(String[] args) {
        byte request = (byte)0x80;
        request |= 3; 
        System.out.println(Integer.toBinaryString(request));
        byte id = 3; //0000 0011
        System.out.println((byte)request | id); //1000 0011  
        long mId = 256L;
        System.out.println(mId >> 8);
        System.out.println(Integer.toBinaryString(0xdabb));// 1101 1010 1011 1011
        System.out.println(Integer.toBinaryString((short)0xdabb));
    }
    
}
