package com.alibaba.dubbo.remoting.transport.support.handler;

import java.util.concurrent.ExecutorService;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ExecutionException;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.support.handler.ChannelEventRunnable.ChannelState;

public class DefaultChannelHandler extends WrappedChannelHandler {
	
	
	
	public DefaultChannelHandler(ChannelHandler handler, URL url) {
		super(handler, url);
	}
	//只有sent方法没有被重载
	
	public void connected(Channel channel) throws RemotingException{
		ExecutorService cexecutor = getExecutorService();
		try {
			cexecutor.execute(new ChannelEventRunnable(channel,handler,ChannelState.CONNECTED));
		} catch (Throwable t) {
			throw new ExecutionException("connect event",channel,getClass() + " error when process connected event.");
		}
	}
	
	@Override
	public void disconnected(Channel channel) throws RemotingException{
		ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.DISCONNECTED));
        }catch (Throwable t) {
            throw new ExecutionException("disconnect event", channel, getClass()+" error when process disconnected event ." , t);
        }
	}
	
	public void received(Channel channel, Object message) throws RemotingException {
        ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.RECEIVED, message));
        }catch (Throwable t) {
            throw new ExecutionException(message, channel, getClass()+" error when process received event ." , t);
        }
    }
	
	public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CAUGHT, exception));
        }catch (Throwable t) {
            throw new ExecutionException("caught event", channel, getClass()+" error when process caught event ." , t);
        }
    }
	
	
	private ExecutorService getExecutorService(){
		ExecutorService cexecutor = executor;
		if(cexecutor == null || cexecutor.isShutdown()){
			cexecutor = SHARED_EXECUTOR;
		}
		return cexecutor;
	}

}
