package com.alibaba.dubbo.remoting.transport.support.handler;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.transport.support.ChannelHandlerWrapper;

@Extension(DefaultChannelHandlerWrapper.NAME)
public class DefaultChannelHandlerWrapper implements ChannelHandlerWrapper {
	public static final String NAME = "default";
	
	public ChannelHandler wrap(ChannelHandler handler,URL url){
		return new DefaultChannelHandler(handler,url);
	}
}
