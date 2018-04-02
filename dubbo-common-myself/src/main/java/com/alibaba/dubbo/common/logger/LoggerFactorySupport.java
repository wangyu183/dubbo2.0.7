package com.alibaba.dubbo.common.logger;

import java.io.File;

/**
 * 日志输出器供给器
 * 工厂模式具体有三种（简单工厂，工厂方法，抽象工厂）
 * 工厂方法模式（有logger接口，有loggerFactorySupport接口）具体的LoggerFactory工厂类对应具体的Logger接口类，
 *              |           |
 *              |           |
 *          JdkLogger,  JdkLoggerFactory       
 *          Log4jLogger Log4jLoggerFactory
 * in my opinion,this is a abstract factory that create logger
 * @author wangyu
 *
 */
public interface LoggerFactorySupport {

    Logger getLogger(Class<?> key);
    
    Logger getLogger(String key);
    
    void setLevel(Level level);
    
    Level getLevel();
    
    File getFile();
    
    void setFile(File file);
    
}
