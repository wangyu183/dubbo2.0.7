package com.alibaba.dubbo.remoting.exchange;

public interface ResponseCallback {
    
    void done(Object response);
    
    void caught(Throwable exception);
}
