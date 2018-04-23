package com.alibaba.dubbo.rpc.wangyu;

import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;

public class ProtocolTest {

    <T> Exporter<T> export(Invoker<T> arg0) throws RpcException {
        if (arg0 == null) {
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
        }
        if (arg0.getUrl() == null) {
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");
        }
        com.alibaba.dubbo.common.URL url = arg0.getUrl();
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null) {
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url("
                    + url.toString() + ") use keys([protocol])");
        }
        com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol) com.alibaba.dubbo.common.ExtensionLoader
                .getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension(extName);
        return extension.export(arg0);
    }

    <T> Invoker<T> refer(Class<T> arg0, URL arg1) throws RpcException {
        if (arg1 == null) {
            throw new IllegalArgumentException("url == null");
        }
        com.alibaba.dubbo.common.URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null) {
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url("
                    + url.toString() + ") use keys([protocol])");
        }
        com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol) com.alibaba.dubbo.common.ExtensionLoader
                .getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension(extName);
        return extension.refer(arg0, arg1);
    }

    public static void main(String[] args) {
        Protocol porotocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

    }
}
