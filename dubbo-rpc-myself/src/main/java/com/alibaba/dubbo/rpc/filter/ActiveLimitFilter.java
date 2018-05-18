package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcStatus;

@Extension("activelimit")
public class ActiveLimitFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        int max = invoker.getUrl().getMethodIntParameter(methodName, Constants.ACTIVES_KEY);
        RpcStatus count = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName());
        if (max > 0) {
            long timeout = invoker.getUrl().getMethodIntParameter(invocation.getMethodName(), Constants.TIMEOUT_KEY);
            long start = System.currentTimeMillis();
            long remain = timeout;
            int active = count.getActive();
            if (active >= max) {
                synchronized (count) {
                    active = count.getActive();
                    while (active >= max) {
                        try {
                            count.wait(remain);
                        } catch (InterruptedException e) {
                        }
                        long elapsed = System.currentTimeMillis() - start;
                        remain = timeout - elapsed;
                        if (remain <= 0) {
                            throw new RpcException("Waiting concurrent invoke timeout in client-side for service:  "
                                                   + invoker.getInterface().getName() + ", method: "
                                                   + invocation.getMethodName() + ", elapsed: " + elapsed
                                                   + ", timeout: " + timeout + ". concurrent invokes: " + active
                                                   + ". max concurrent invoke limit: " + max);
                        }
                    }
                }
            }
        }
        try {
            long begin = System.currentTimeMillis();
            RpcStatus.beginCount(url, methodName);
            try {
                Result result = invoker.invoke(invocation);
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, true);
                return result;
            } catch (RuntimeException t) {
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, false);
                throw t;
            }
        } finally {
            if(max>0){
                synchronized (count) {
                    count.notify();
                } 
            }
        }
    } 
}
