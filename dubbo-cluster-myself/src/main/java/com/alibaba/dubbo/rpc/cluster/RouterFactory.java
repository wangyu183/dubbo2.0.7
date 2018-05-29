package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;

/**
 * RouterFactory.(SPI, Singleton, ThreadSafe)
 * @author wangyu
 *
 */
@Extension
public interface RouterFactory {

    @Adaptive("protocol")
    Router getRouter(URL url);
}
