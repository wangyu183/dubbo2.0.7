package com.alibaba.dubbo.remoting.transport.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;

public abstract class AbstractChannel extends AbstractPeer implements Channel{

	public AbstractChannel(URL url, ChannelHandler handler) {
		super(url, handler);
	}
	
	public void send(Object message,boolean sent) throws RemotingException{
		if(isClosed()){
			  throw new RemotingException(this, "Failed to send message "
                      + (message == null ? "" : message.getClass().getName()) + ":" + message
                      + ", cause: Channel closed. channel: " + getLocalAddress() + " -> " + getRemoteAddress());
		}
	}
	
	@Override
	public String toString(){
		return getLocalAddress() + " -> " + getRemoteAddress();
	}

}
