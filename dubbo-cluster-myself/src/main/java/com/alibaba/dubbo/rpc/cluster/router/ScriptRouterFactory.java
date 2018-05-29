package com.alibaba.dubbo.rpc.cluster.router;

import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

@Extension(ScriptRouterFactory.NAME)
public class ScriptRouterFactory implements RouterFactory {
    
    public static final String NAME = "script";
    
    public Router getRouter(URL url) {
        return new ScriptRouter(url);
    }

}
