package com.alibaba.dubbo.rpc;

import java.io.Serializable;

public class RpcResult implements Result, Serializable {

    private static final long serialVersionUID = 2690347276035668685L;

    private Object result;
    
    private Throwable exception;
    
    public RpcResult() {}
    
    public RpcResult(Object result) {
        this.result = result;
    }
    
    public RpcResult(Throwable exception) {
        this.exception = exception;
    }
    
    public boolean hasException() {
        return exception != null;
    }

    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }
    
    public void setException(Throwable e) {
        this.exception = e;
    }

    public Object recreate() throws Throwable {
        if(exception != null) {
            throw exception;
        }
        return result;
    }

}
