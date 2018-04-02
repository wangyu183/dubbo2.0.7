package com.alibaba.dubbo.common.logger.support;

import com.alibaba.dubbo.common.logger.Logger;

public class FailsafeLogger implements Logger{
    
    private final Logger logger;
    
    public FailsafeLogger(Logger logger) {
        this.logger = logger;
    }
    
    public void trace(String msg) {
        try {
            logger.trace(msg);
        } catch (Throwable t) {
        }
        
    }

    public void trace(Throwable e) {
        try {
            logger.trace(e);
        } catch (Throwable t) {
        }
    }

    public void trace(String msg, Throwable e) {
        try {
            logger.trace(msg,e);
        } catch (Throwable t) {
        }
    }

    public void debug(String msg) {
        try {
            logger.debug(msg);
        } catch (Throwable t) {
        }
    }

    public void debug(Throwable e) {
        try {
            logger.debug(e);
        } catch (Throwable t) {
        }
    }

    public void debug(String msg, Throwable e) {
        try {
            logger.debug(msg,e);
        } catch (Throwable t) {
        }
    }

    public void info(String msg) {
        try {
//            Throwable throwable = new Throwable();
//            StackTraceElement[] ste = throwable.getStackTrace();
//            for (StackTraceElement stackTraceElement : ste) {
//               System.out
//                     .println("ClassName: " + stackTraceElement.getClassName());
//               System.out.println("Method Name: "
//                     + stackTraceElement.getMethodName());
//               System.out.println("Line number: "
//                     + stackTraceElement.getLineNumber());
//            }
            logger.info(msg);
        } catch (Throwable t) {
        }
    }

    public void info(Throwable e) {
        try {
            logger.info(e);
        } catch (Throwable t) {
        }
    }

    public void info(String msg, Throwable e) {
        try {
            logger.info(msg,e);
        } catch (Throwable t) {
        }
    }

    public void warn(String msg) {
        try {
            logger.warn(msg);
        } catch (Throwable t) {
        }
    }

    public void warn(Throwable e) {
        try {
            logger.warn(e);
        } catch (Throwable t) {
        }
    }

    public void warn(String msg, Throwable e) {
        try {
            logger.warn(msg,e);
        } catch (Throwable t) {
        }
    }

    public void error(String msg) {
        try {
            logger.error(msg);
        } catch (Throwable t) {
        }
    }

    public void error(Throwable e) {
        try {
            logger.error(e);
        } catch (Throwable t) {
        }
    }

    public void error(String msg, Throwable e) {
        try {
            logger.error(msg,e);
        } catch (Throwable t) {
        }
    }

    public boolean isTraceEnabled() {
        try {
            return logger.isTraceEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isDebugEnabled() {
        try {
            return logger.isDebugEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isInfoEnabled() {
        try {
            return logger.isInfoEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isWarnEnabled() {
        try {
            return logger.isWarnEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

}
