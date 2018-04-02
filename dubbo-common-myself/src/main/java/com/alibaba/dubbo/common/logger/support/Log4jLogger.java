package com.alibaba.dubbo.common.logger.support;

import org.apache.log4j.Level;

import com.alibaba.dubbo.common.logger.Logger;

public class Log4jLogger implements Logger {
    
    /**
     * <p>包装器类的完全限定类名,Log4j通过它获取正确调用的class信息</p>
     * @see <a href="http://www.coderli.com/log4j-slf4j-logger-linenumber/">log4j根据FQCN遍历堆栈获得调用类的信息</a>
     */
    private static final String FQCN = FailsafeLogger.class.getName();
    
    private final org.apache.log4j.Logger logger;
    
    public Log4jLogger(org.apache.log4j.Logger logger) {
        this.logger = logger;
    }
    
    public void trace(String msg) {
        logger.log(FQCN, Level.TRACE, msg, null);

    }

    public void trace(Throwable e) {
        logger.log(FQCN, Level.TRACE, e == null ? null : e.getMessage(), e);
        
    }

    public void trace(String msg, Throwable e) {
        logger.log(FQCN, Level.TRACE, msg, e);
    }

    public void debug(String msg) {
        logger.log(FQCN, Level.DEBUG, msg, null);
    }

    public void debug(Throwable e) {
        logger.log(FQCN, Level.DEBUG, e == null ? null : e.getMessage(), e);
    }

    public void debug(String msg, Throwable e) {
        logger.log(FQCN, Level.DEBUG, msg, e);
    }

    public void info(String msg) {
//        Throwable throwable = new Throwable();
//        StackTraceElement[] ste = throwable.getStackTrace();
//        for (StackTraceElement stackTraceElement : ste) {
//           System.out
//                 .println("ClassName: " + stackTraceElement.getClassName());
//           System.out.println("Method Name: "
//                 + stackTraceElement.getMethodName());
//           System.out.println("Line number: "
//                 + stackTraceElement.getLineNumber());
//        }
        logger.log(FQCN, Level.INFO, msg, null);
    }

    public void info(Throwable e) {
        logger.log(FQCN, Level.INFO, e == null ? null : e.getMessage(), e);
    }

    public void info(String msg, Throwable e) {
        logger.log(FQCN, Level.INFO, msg, e);
    }

    public void warn(String msg) {
        logger.log(FQCN, Level.WARN, msg, null);
    }

    public void warn(Throwable e) {
        logger.log(FQCN, Level.WARN, e == null ? null : e.getMessage(), e);
    }

    public void warn(String msg, Throwable e) {
        logger.log(FQCN, Level.WARN, msg, e);
    }

    public void error(String msg) {
        logger.log(FQCN, Level.ERROR, msg, null);
    }

    public void error(Throwable e) {
        logger.log(FQCN, Level.ERROR, e == null ? null : e.getMessage(), e);
    }

    public void error(String msg, Throwable e) {
        logger.log(FQCN, Level.ERROR, msg, e);
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

}
