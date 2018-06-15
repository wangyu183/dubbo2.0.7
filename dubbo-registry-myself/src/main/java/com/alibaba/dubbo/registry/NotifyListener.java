package com.alibaba.dubbo.registry;

import java.util.List;

import com.alibaba.dubbo.common.URL;

/**
 * API, Prototype, ThreadSafe
 * @author wangyu
 *
 */
public interface NotifyListener {

    void notify(List<URL> urls);
}
