package com.alibaba.dubbo.rpc.service;

import com.alibaba.dubbo.common.utils.StringUtils;

public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -5840551875241247451L;
    
    private String exceptionClass;
    
    private String exceptionMessage;
    
    public GenericException() {
        super();
    }
    
    public GenericException(Throwable cause) {
        super(StringUtils.toString(cause));
        this.exceptionClass = cause.getClass().getName();
        this.exceptionMessage = cause.getMessage();
    }
    
    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
    
}
