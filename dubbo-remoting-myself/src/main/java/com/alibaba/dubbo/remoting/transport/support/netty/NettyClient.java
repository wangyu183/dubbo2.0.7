package com.alibaba.dubbo.remoting.transport.support.netty;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.support.AbstractClient;

public class NettyClient extends AbstractClient {

	public NettyClient(URL url, ChannelHandler handler) throws RemotingException {
		super(url, handler);
		// TODO Auto-generated constructor stub
	}
	
	protected static ChannelHandler wrapChannelHandler(URL url,ChannelHandler handler){
		 url = url.addParameter(Constants.THREAD_NAME_KEY, CLIENT_THREAD_POOL_NAME)
		            .addParameter(Constants.THREADPOOL_KEY, Constants.DEFAULT_CLIENT_THREADPOOL);
	}

	public void received(Channel channel, Object message) throws RemotingException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doOpen() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doClose() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doConnect() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDisConnect() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected Channel getChannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
