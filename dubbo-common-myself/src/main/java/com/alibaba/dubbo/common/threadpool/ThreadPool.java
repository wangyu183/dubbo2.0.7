package com.alibaba.dubbo.common.threadpool;

import java.util.concurrent.Executor;

import com.alibaba.dubbo.common.Adaptive;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;

@Extension("fixed")
public interface ThreadPool {
    
    @Adaptive({Constants.THREADPOOL_KEY})
    Executor getExecutor(URL url);
}
