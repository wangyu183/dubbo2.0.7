package com.alibaba.dubbo.common;


import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

public class LogTest {
    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);
    
    public static void main(String[] args) {
        logger.info("输出重要信息");
    }
}
