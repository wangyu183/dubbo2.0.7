package com.alibaba.dubbo.remoting.transport.support.netty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
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
	
	private volatile Channel channel;
	
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
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(),getUrl(),NettyClient.this);
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder());
                pipeline.addLast("encoder", adapter.getEncoder());
                pipeline.addLast("handler", nettyHandler);
                return pipeline;
            }
        });
	}

	@Override
	protected void doClose() throws Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doConnect() throws Throwable {
		// TODO Auto-generated method stub
	    ChannelFuture future = bootstrap.connect(getConnectAddress());
	    try {
            long start = System.currentTimeMillis();
            final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
            final CountDownLatch finish = new CountDownLatch(1);
            future.addListener(new ChannelFutureListener() {
                
                public void operationComplete(ChannelFuture future) throws Exception {
                    try {
                        if(future.isSuccess()) {
                            Channel newChannel = future.getChannel();
                            newChannel.setInterestOps(Channel.OP_READ_WRITE);
                            try {
                                Channel oldChannel = NettyClient.this.channel;
                                if(oldChannel != null) {
                                    try {
                                        if(logger.isInfoEnabled()) {
                                            logger.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                                        }
                                        oldChannel.close();
                                    } finally {
                                        NettyChannel.removeChannelIfDisconnected(oldChannel);
                                    }
                                }
                            } finally {
                                if(NettyClient.this.isClosed()) {
                                    try {
                                        if (logger.isInfoEnabled()) {
                                            logger.info("Close new netty channel " + newChannel + ", because the client closed.");
                                        }
                                        newChannel.close();
                                    } finally {
                                        //疑问 何时newChannel保存在NettyChannel的map中的？
                                        NettyClient.this.channel = null;
                                        NettyChannel.removeChannelIfDisconnected(newChannel);
                                    }
                                }else {
                                    NettyClient.this.channel = newChannel;
                                }
                            }
                        }else if(future.getCause() != null) {
                            exception.set(future.getCause());
                        }
                    } catch (Exception e) {
                        exception.set(future.getCause());
                    }finally {
                        finish.countDown();
                    }
                }
            });
            try {
                finish.await(getConnectTimeout(),TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RemotingException(this, "Failed to connect to server " + getRemoteAddress() + " client-side timeout "
                        + getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start)
                        + "ms) from netty client " + NetUtils.getLocalHost() + " using dubbo version "
                        + Version.getVersion() + ", cause: " + e.getMessage(), e);
            }
            Throwable e = exception.get();
            if (e != null) {
                throw e;
            }
        } finally {
            if (! isConnected()) {
                future.cancel();
            }
        }
	}

	@Override
	protected void doDisConnect() throws Throwable {
	    try {
            NettyChannel.removeChannelIfDisconnected(channel);
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
	}

	@Override
	protected com.alibaba.dubbo.remoting.Channel getChannel() {
	    Channel c = channel;
	    if(c == null || !c.isConnected()) {
	        return null;
	    }
		return NettyChannel.getOrAddChannel(c, getUrl(), this);
	}

}
