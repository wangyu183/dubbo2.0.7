package com.alibaba.dubbo.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.dubbo.common.URL;

public class RpcStatus {
    
    private static final ConcurrentMap<URL, RpcStatus> SERVICE_STATISTICS = new ConcurrentHashMap<URL, RpcStatus>();
    
    private static final ConcurrentMap<URL, ConcurrentMap<String, RpcStatus>> METHOD_STATISTICS = new ConcurrentHashMap<URL, ConcurrentMap<String, RpcStatus>>();
    
    public static RpcStatus getStatus(URL url) {
        RpcStatus status = SERVICE_STATISTICS.get(url);
        if(status == null) {
            SERVICE_STATISTICS.putIfAbsent(url, new RpcStatus());
            status = SERVICE_STATISTICS.get(url);
        }
        return status;
    }
    
    public static void removeStatus(URL url) {
        SERVICE_STATISTICS.remove(url);
    }
    
    public static RpcStatus getStatus(URL url, String methodName) {
        ConcurrentMap<String, RpcStatus> map = METHOD_STATISTICS.get(url);
        if(map == null) {
            METHOD_STATISTICS.putIfAbsent(url, new ConcurrentHashMap<String, RpcStatus>());
            map = METHOD_STATISTICS.get(url);
        }
        RpcStatus status = map.get(methodName);
        if (status == null) {
            map.putIfAbsent(methodName, new RpcStatus());
            status = map.get(methodName);
        }
        return status;
    }
    
    public static void removeStatus(URL url, String methodName) {
        ConcurrentMap<String, RpcStatus> map = METHOD_STATISTICS.get(url);
        if (map != null) {
            map.remove(methodName);
        }
    }
    
    public static void beginCount(URL url, String methodName) {
        beginCount(getStatus(url));
        beginCount(getStatus(url, methodName));
    }
    
    public static void beginCount(RpcStatus status) {
        status.active.incrementAndGet();
    }
    
    public static void endCount(URL url, String methodName, long elapsed, boolean succeeded) {
        endCount(getStatus(url), elapsed, succeeded);
        endCount(getStatus(url, methodName), elapsed, succeeded);
    }
    
    private static void endCount(RpcStatus status, long elapsed, boolean succeeded) {
        status.active.decrementAndGet();
        status.total.incrementAndGet();
        status.totalElapsed.addAndGet(elapsed);
        if (status.maxElapsed.get() < elapsed) {
            status.maxElapsed.set(elapsed);
        }
        if (succeeded) {
            if (status.succeededMaxElapsed.get() < elapsed) {
                status.succeededMaxElapsed.set(elapsed);
            }
        } else {
            status.failed.incrementAndGet();
            status.failedElapsed.addAndGet(elapsed);
            if (status.failedMaxElapsed.get() < elapsed) {
                status.failedMaxElapsed.set(elapsed);
            }
        }
    }
    
    private final AtomicInteger active = new AtomicInteger();
    
    private final AtomicLong total = new AtomicLong();

    private final AtomicInteger failed = new AtomicInteger();

    private final AtomicLong totalElapsed = new AtomicLong();

    private final AtomicLong failedElapsed = new AtomicLong();

    private final AtomicLong maxElapsed = new AtomicLong();

    private final AtomicLong failedMaxElapsed = new AtomicLong();

    private final AtomicLong succeededMaxElapsed = new AtomicLong();
    
    private volatile boolean ready = true;

    private RpcStatus() {}
    
    public int getActive() {
        return active.get();
    }
    
}
