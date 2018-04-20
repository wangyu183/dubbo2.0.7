package com.alibaba.dubbo.remoting.transport.support.handler;

import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ChannelHandlerWrapper;

public class ChannelHandlers {
	
	public static ChannelHandler wrap(ChannelHandler handler,URL url){
		return ExtensionLoader.getExtensionLoader(ChannelHandlerWrapper.class).getAdaptiveExtension().wrap(handler, url);
	}
}
