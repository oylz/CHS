package com.mz.schudlerserver.task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mz.schudlerserver.common.ConfigParams;


public class CheckTaskPool{
	private static CheckTaskPool instance = null;
	private ThreadPoolExecutor threadPool = null;
	private CheckTaskPool(){
		SchudlerServerThreadFactory factory = new SchudlerServerThreadFactory("CheckTaskPool");
		threadPool = new ThreadPoolExecutor(ConfigParams.corePoolSize, 
				ConfigParams.maximumPoolSize, 
				ConfigParams.keepAliveTime, 
				TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(),
				factory,
	            new ThreadPoolExecutor.AbortPolicy());		
	}
	static public CheckTaskPool getInstance(){
		 if(instance == null){
	            synchronized (CheckTaskPool.class){
	                if(instance == null){
	                    instance = new CheckTaskPool();
	                }
	            }
	        }
	        return instance;
	}
	public void commitTask(Runnable task){
		threadPool.execute(task);
	}
	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}
	
	public void shutdown(){
		threadPool.shutdown();
	}
}