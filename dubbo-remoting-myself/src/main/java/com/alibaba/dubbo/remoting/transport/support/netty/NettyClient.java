package com.alibaba.dubbo.remoting.transport.support.netty;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.support.AbstractClient;
import com.alibaba.dubbo.remoting.transport.support.handler.ChannelHandlers;

public class NettyClient extends AbstractClient {
	
	//DEFAULT_IO_THREADS 默认的线程数量 如4核 则返回4 双核每个有两个超线程 则也返回4
	private static final ChannelFactory channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientBoss",true)), 
			Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientWorker",true)), 
			Constants.DEFAULT_IO_THREADS);
	
	private ClientBootstrap bootstrap;
	
	public NettyClient(URL url, ChannelHandler handler) throws RemotingException {
		super(url, handler);
		// TODO Auto-generated constructor stub
	}
	
	protected static ChannelHandler wrapChannelHandler(URL url,ChannelHandler handler){
		 url = url.addParameter(Constants.THREAD_NAME_KEY, CLIENT_THREAD_POOL_NAME)
		            .addParameter(Constants.THREADPOOL_KEY, Constants.DEFAULT_CLIENT_THREADPOOL);
		 return ChannelHandlers.wrap(handler, url);
	}

	public void received(Channel channel, Object message) throws RemotingException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doOpen() throws Throwable {
		// TODO Auto-generated method stub
		bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("connectTimeoutMillis", getTimeout());
		final NettyHandler nettyHandler = new NettyHandler(getUrl(),this);
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
