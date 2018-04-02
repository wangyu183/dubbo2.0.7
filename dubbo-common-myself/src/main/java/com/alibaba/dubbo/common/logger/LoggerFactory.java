package com.alibaba.dubbo.common.logger;

import java.io.File;

import com.alibaba.dubbo.common.logger.support.FailsafeLogger;
import com.alibaba.dubbo.common.logger.support.Log4jLoggerFactory;

/**
 * 
 * @author wangyu
 *
 */
public class LoggerFactory {
    
    
    private LoggerFactory() {}
    
    private static volatile LoggerFactorySupport LOGGER_FACTORY;
    
    static {
        setLoggerFactory(new Log4jLoggerFactory());
    }
    
    public static void setLoggerFactory(LoggerFactorySupport loggerFactory) {
        if(loggerFactory != null) {
            Logger logger = loggerFactory.getLogger(LoggerFactory.class.getName());
            logger.info("using logger: " + loggerFactory.getClass().getName());
            LoggerFactory.LOGGER_FACTORY = loggerFactory;
        }
    }
    
    public static Logger getLogger(Class<?> key) {
        return new FailsafeLogger(LOGGER_FACTORY.getLogger(key));
    }
    
    public static Logger getLogger(String key) {
        return new FailsafeLogger(LOGGER_FACTORY.getLogger(key));
    }
    
    /**
     * 动态设置输出日志级别
     * 
     * @param level 日志级别
     */
    public static void setLevel(Level level) {
        LOGGER_FACTORY.setLevel(level);
    }

    /**
     * 获取日志级别
     * 
     * @return 日志级别
     */
    public static Level getLevel() {
        return LOGGER_FACTORY.getLevel();
    }
    
    /**
     * 获取日志文件
     * 
     * @return 日志文件
     */
    public static File getFile() {
        return LOGGER_FACTORY.getFile();
    }
    
}
