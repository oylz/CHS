package com.mz.schudlerserver.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.common.ConfigParams;
import com.mz.schudlerserver.common.GlobalParams;
import com.mz.schudlerserver.model.HaproxyModel;
import com.mz.schudlerserver.util.ConsistentHashUtil;
import com.mz.schudlerserver.util.HaProxyAddrsUtil;


public class HaproxyHealthCheckTask implements Runnable{
	 private static final Logger logger = LoggerFactory.getLogger(HaproxyHealthCheckTask.class);
	 public static String checkedReultOk = "succeeded";
	 SimpleDateFormat formatter = new SimpleDateFormat("yyyy年-MM月dd日-HH时mm分ss秒SSS毫秒");
	 public static Map<String, String> downHaproxyMap = new ConcurrentHashMap<>();
	 private String node;
	 //private Timer timer_ = new Timer(true);
	public HaproxyHealthCheckTask(String node) {
		super();
		this.node = node;
	}
	private class CTask  extends TimerTask{
		@Override
		public void run() {
			checkHealth(node);
		}
		
	}
	@Override
	public void run() {
//		CTask task = new CTask();
//		timer_.schedule(task, 0, ConfigParams.checkHealthTimeInterval);
		while(GlobalParams.THREAD_RUNNING_YES.equals(ConfigParams.checkHealthRunning)){
			try {
				checkHealth(node);
				Thread.sleep(ConfigParams.checkHealthTimeInterval);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	/**
	 * haproxy健康检测
	 * @param node
	 */
	private void checkHealth(String node){
			 HaproxyModel haproxyModel = HaProxyAddrsUtil.getInstance().haproxyMap.get(node);
			 boolean isNotHealth = executeNcShell(haproxyModel,0,node);
			 if(isNotHealth){
				 removeDownHaproxyNodeFromHash(node);
			 }else{
				 reAddUpHaproxyNodeToHash(node); 
			 }
	}
	
	
	/**
	 * 调用shell执行nc检测haproxy端口
	 * @param haproxyModel
	 * @param tryCheckTimes 重试检测次数
	 * @param node 节点
	 */
	private boolean executeNcShell(HaproxyModel haproxyModel,int tryCheckTimes,String node){
		boolean isNotHealth = false;
		String ip = haproxyModel.getIp();
		String port = haproxyModel.getPort();
		String shellCmd = "/usr/bin/nc -z -w 1 "+ip+" "+port;
		try {
			Process process = Runtime.getRuntime().exec(shellCmd);
			StringBuffer processResult = new StringBuffer();
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));  
            String line = "";  
            while ((line = input.readLine()) != null) {  
            	processResult.append(line);  
            }  
            input.close();  
			int exitValue = process.waitFor();
			if (0 == exitValue) {
			    if(!processResult.toString().contains(checkedReultOk)){
			    	//logger.error("haproxy----ip:"+ip+";port:"+port+" is down");
			    	isNotHealth = true;
			    }
				
			}else{
				//logger.error("call shell failed. error code is :" + exitValue);
				isNotHealth = true;
			}
		} catch (Throwable e) {
			logger.error("call shell failed. "+e.getMessage(),e);
		}
		
		//如果检测到不健康则进行重试,避免误判
		if(isNotHealth){
			if(!downHaproxyMap.containsKey(node)){
				tryCheckTimes++;
				if(tryCheckTimes > ConfigParams.notHealthTotalRetryTimes){
					return isNotHealth;
				}
				logger.info("当前时间: " + formatter.format(new Date()) + ","
						+ " 检测到:haproxy("+node+")疑似宕机，将进行第"+tryCheckTimes+"次重试检测");
				try {
					Thread.sleep(ConfigParams.notHealthTimeInterval);
					return executeNcShell(haproxyModel, tryCheckTimes,node);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		return isNotHealth;
		
		
	}
	
	/**
	 * 从一致性hash环删除宕机节点,并将宕机节点加入宕机map
	 */
	private void removeDownHaproxyNodeFromHash(String node){
		if(!downHaproxyMap.containsKey(node)){
			logger.error("当前时间: " + formatter.format(new Date()) + 
					", 检测到:haproxy("+node+")已经宕机，将从一致性HASH环删除该节点");
		  ConsistentHashUtil.getInstance().remove(node);
		  downHaproxyMap.put(node, "down");
		}
	
	}
	
	/**
	 * 从一致性hash环添加已经恢复正常的节点,并把该节点从宕机map中移除
	 */
	private void reAddUpHaproxyNodeToHash(String node){
	 if(downHaproxyMap.containsKey(node)){
		 logger.info("当前时间: " + formatter.format(new Date()) + 
					", 检测到:haproxy("+node+")已经恢复正常，一致性HASH环将重新添加该节点"); 
		 ConsistentHashUtil.getInstance().add(node);
		 downHaproxyMap.remove(node);
	 }
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
	
	
	
	
	
	
	
	
   
	
	

}
