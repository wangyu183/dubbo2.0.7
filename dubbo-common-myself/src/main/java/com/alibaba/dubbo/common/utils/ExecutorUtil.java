package com.alibaba.dubbo.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

public class ExecutorUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtil.class);
    
    private static final ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(0,1,0L,TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new NamedThreadFactory("Close-ExecutorService-Timer", true));
    
    public static boolean isShutdown(Executor executor) {
        if(executor instanceof ExecutorService) {
            if(((ExecutorService) executor).isShutdown()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * graceful 优美的
     * @param executor
     * @param timeout
     */
    public static void gracefulShutdown(Executor executor,int timeout) {
        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            es.shutdown(); // Disable new tasks from being submitted
        } catch (SecurityException ex2) {
            return ;
        } catch (NullPointerException ex2) {
            return ;
        }
        try {
            if(! es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException ex) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (!isShutdown(es)){
            newThreadToCloseExecutor(es ,timeout);
        }
    }
    
    public static void shutdownNow(Executor executor,final int timeout) {
        if(!(executor instanceof ExecutorService) ) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        
        /**
         * shutdown方法：平滑的关闭ExecutorService，当此方法被调用时，
         * ExecutorService停止接收新的任务并且等待已经提交的任务（包含提交正在执行和提交未执行）执行完成。
         * 当所有提交任务执行完毕，线程池即被关闭。awaitTermination方法：接收人timeout和TimeUnit两个参数，用于设定超时时间及单位。
         * 当等待超过设定时间时，会监测ExecutorService是否已经关闭，若关闭则返回true，否则返回false。一般情况下会和shutdown方法组合使用。
         */
        try {
            es.shutdownNow();
        } catch (SecurityException ex2) {
            return ;
        } catch (NullPointerException ex2) {
            return ;
        }
        
        try {
            es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(!isShutdown(es)) {
            newThreadToCloseExecutor(es,timeout);
        }
    }
    
    private static void newThreadToCloseExecutor(final ExecutorService es,final int timeout) {
        if(!isShutdown(es)) {
            shutdownExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        es.shutdownNow();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                    
                    try {
                        es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }
    
}
