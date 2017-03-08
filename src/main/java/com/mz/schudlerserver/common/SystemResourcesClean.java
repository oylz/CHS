package com.mz.schudlerserver.common;

import com.mz.schudlerserver.netty.server.ConnHttpNettyServer;

import com.mz.schudlerserver.netty.server.ConnHttpsNettyServer;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SystemResourcesClean {

    private static final Logger logger = LoggerFactory.getLogger(SystemResourcesClean.class);

    /**
     *  系统升级时，调用此方法关闭进程入口
     */
    public static synchronized void closeNettyBossGroup(boolean isHttps){
        logger.error("程序退出，开始关闭进程入口");
        EventLoopGroup group = ConnHttpNettyServer.getBoss();
        closeNettyBossGroup(group);
        if(isHttps) {
        	group = ConnHttpsNettyServer.getBoss();
            closeNettyBossGroup(group);
        }
    }
    /**
     *  系统升级时，调用此方法关闭IO
     */
    public static synchronized void closeNettyWorkerGroup(boolean isHttps){
        logger.error("程序退出，开始关闭IO");
        EventLoopGroup group = ConnHttpNettyServer.getWorker();
        closeNettyWorkerGroup(group);
        if(isHttps) {
        	group = ConnHttpsNettyServer.getWorker();
            closeNettyWorkerGroup(group);
        }
    }

    private static void closeNettyBossGroup(EventLoopGroup bossGroup){
    	logger.info("boss:准备退出...");
        if(bossGroup != null){
            try {
				bossGroup.shutdownGracefully().sync();
			} 
            catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        while(true){
            try{
                if(bossGroup == null || bossGroup.awaitTermination(200, TimeUnit.MILLISECONDS)){
                    break;
                }
                logger.info("boss:sleep一会儿");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        logger.info("boss:退出完成...");
        
    }    
    public static void closeNettyWorkerGroup(EventLoopGroup workerGroup){
        logger.info("worker:准备退出...");
        if(workerGroup != null){
            try {
                workerGroup.shutdownGracefully().sync();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(true){
            try{
                if(workerGroup == null || workerGroup.awaitTermination(200, TimeUnit.MILLISECONDS)){
                    break;
                }
                logger.info("worker:sleep一会儿");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        logger.info("worker:退出完成...");
    }    
}
