package com.alibaba.dubbo.rpc.dubbo;

import java.io.IOException;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.exchange.codec.ExchangeCodec;
import com.alibaba.dubbo.rpc.RpcInvocation;

@Extension(DubboCodec.NAME)
public class DubboCodec extends ExchangeCodec implements Codec {

    public static final String NAME = "dubbo";
    
    private static final String     DUBBO_VERSION           = Version.getVersion(DubboCodec.class, Version.getVersion());

    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        RpcInvocation inv = (RpcInvocation) data;
        out.writeUTF(inv.getAttachment(Constants.DUBBO_VERSION_KEY, DUBBO_VERSION));
        out.writeUTF(inv.getAttachment(Constants.PATH_KEY));
        out.writeUTF(inv.getAttachment(Constants.VERSION_KEY));
        out.writeUTF(inv.getMethodName());
        out.writeUTF(ReflectUtils.getDesc(inv.getParameterTypes()));
        
        Object[] args = inv.getArguments();
        if(args != null) {
            for(int i = 0; i < args.length; i++) {
                out.writeObject(obj);
            }
        }
    }
    
    
}
