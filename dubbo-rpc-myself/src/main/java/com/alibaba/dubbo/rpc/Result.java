package com.alibaba.dubbo.rpc;

public interface Result {
    
    boolean hasException();
    
    Object getResult();
    
    Throwable getException();
    
    Object recreate() throws Throwable;
    
}
