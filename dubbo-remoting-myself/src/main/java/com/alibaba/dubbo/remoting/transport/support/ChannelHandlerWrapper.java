package com.alibaba.dubbo.remoting.transport.support;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.transport.support.handler.DefaultChannelHandlerWrapper;

@Extension(DefaultChannelHandlerWrapper.NAME)
public interface ChannelHandlerWrapper {
	
	//channel.handler
	@Adaptive({Constants.CHANNEL_HANDLER_KEY})
	ChannelHandler wrap(ChannelHandler handler,URL url);
}
