package com.alibaba.dubbo.remoting.transport.support.handler;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;

public class DefaultChannelHandler extends WrappedChannelHandler {

	public DefaultChannelHandler(ChannelHandler handler, URL url) {
		super(handler, url);
	}

}
