package com.alibaba.dubbo.remoting.Telnet.support;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.transport.support.ChannelHandlerAdapter;

public class TelnetHandlerAdapter extends ChannelHandlerAdapter implements TelnetHandler {

    public String telnet(Channel channel, String message) throws RemotingException {
        String telnet = channel.getUrl().getParameter("telnet");
        String prompt = channel.getUrl().getParameter("prompt");

        List<String> commands = ConfigUtils.mergeValues(TelnetHandler.class, telnet, Constants.DEFAULT_TELNET_COMMANDS);
        StringBuilder buf = new StringBuilder();
        if (commands != null && commands.size() > 0) {
            message = message.trim();
            String command;
            if (message.length() > 0) {
                int i = message.indexOf(' ');
                if (i > 0) {
                    command = message.substring(0, i).trim();
                    message = message.substring(i + 1).trim();
                } else {
                    command = message;
                    message = "";
                }
            } else {
                command = "";
            }
            if (commands.contains(command)) {
                TelnetHandler handler = ExtensionLoader.getExtensionLoader(TelnetHandler.class).getExtension(command);
                try {
                    String result = handler.telnet(channel, message);
                    if (result != null) {
                        buf.append(result);
                    }
                } catch (Throwable t) {
                    buf.append(t.getMessage());
                }
            } else if (command.length() > 0) {
                buf.append("Unsupported command: ");
                buf.append(command);
            }
            if (buf.length() > 0) {
                buf.append("\r\n");
            }
        }
        if (prompt != null && prompt.length() > 0) {
            buf.append(prompt);
            buf.append(">");
        }
        return buf.toString();
    }
}
