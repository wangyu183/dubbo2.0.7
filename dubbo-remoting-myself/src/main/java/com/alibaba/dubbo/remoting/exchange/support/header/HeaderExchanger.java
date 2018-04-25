package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;

@Extension(HeaderExchanger.NAME)
public class HeaderExchanger {

    public static final String NAME = "header";
    
    public ExchangeClient connect(URL url, ExchangeHandler handler) {
        
    }
    
    public ExchangeServer bind(URL url, ExchangeHandler handler) {
        
    }
    
}
