package com.mz.schudlerserver.main;

import java.util.Map;

import com.mz.schudlerserver.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.common.ConfigParams;
import com.mz.schudlerserver.common.SystemResourcesClean;
import com.mz.schudlerserver.model.HaproxyModel;
import com.mz.schudlerserver.netty.connhandler.HolderConnHandler;
import com.mz.schudlerserver.netty.server.ConnHttpNettyServer;
import com.mz.schudlerserver.netty.server.ConnHttpsNettyServer;
import com.mz.schudlerserver.task.CheckTaskPool;
import com.mz.schudlerserver.task.HaproxyHealthCheckTask;
import com.mz.schudlerserver.util.ConsistentHashUtil;
import com.mz.schudlerserver.util.HaProxyAddrsUtil;
import sun.misc.Signal;
import sun.misc.SignalHandler;


public class SchudlerServerMain {

    private static final Logger logger = LoggerFactory.getLogger(SchudlerServerMain.class);
    private static final boolean SSL = System.getProperty("ssl") != null;

    public static void main(String args[]) {
        boolean isHttps = SSL || args.length>0 && args[0].equals("ssl");
        if(args.length < 0){
        	System.out.println("useage:\n\tjava -jar ss.jar type(0[handler]、1[sche task]、2[task poll])\n");
        	return;
        }
        //监听捕获信号量
        SignalHandler handler = signal -> {
            //捕获到信号量，进行具体业务关闭处理
            try {
                shutdownCallback(isHttps);
            } catch (Exception e) {
                logger.error("捕获到信号量，进行具体业务关闭处理失败" + e.toString());
            }
        };
        //注册kill -15信号量
        Signal.handle(new Signal(ConfigParams.killSignal), handler);

    	ConsistentHashUtil.getInstance().init(ConfigParams.numberOfReplicas, ConfigParams.haproxyAddrs);
        ConnHttpNettyServer nettyServer = new ConnHttpNettyServer();
        Thread nettyThread = new Thread(nettyServer);
        nettyThread.start();
        if (isHttps) {
        	ConnHttpsNettyServer httpsServer = new ConnHttpsNettyServer();
            Thread httpsThread = new Thread(httpsServer);
            httpsThread.start();
        }
        
        initCheckHaproxyHealthTask();
        try {
            nettyThread.join();
        } catch (InterruptedException e) {
            logger.error("子线程意外中断，系统将退出" + e.getMessage(), e);
            System.exit(-1);
        } 
//        finally {
//            SystemResourcesClean.closeAllResources();
//        }
    }
    
    
    private static void initCheckHaproxyHealthTask(){
    	  Map<String, HaproxyModel> checkHaproxyMap = HaProxyAddrsUtil.getInstance().haproxyMap;
          for(String node : checkHaproxyMap.keySet()){
        	  HaproxyHealthCheckTask task = new HaproxyHealthCheckTask(node); 
        	  CheckTaskPool.getInstance().commitTask(task);
          }
          
    }

    /**
     * 阻止新的请求，等旧的请求处理完再关闭进程
     * 用于不间断升级
     */
    private static void shutdownCallback(boolean isHttps) throws Exception {
        logger.info("1、开启iptables,踢SYN包");
        ShellUtil.shellExecute(ConfigParams.shellPathSt1, true);
        logger.info("2、关闭进程入口");
        SystemResourcesClean.closeNettyBossGroup(isHttps);
        logger.info("3、启动新进程");
        startNewProcess();
        logger.info("4、检查进程是否已经启动成功，并关闭iptables");
        ShellUtil.shellExecute(ConfigParams.shellPathSt2, true);
        logger.info("5、关闭IO");
        SystemResourcesClean.closeNettyWorkerGroup(isHttps);
        logger.info("6、关闭haproxy检测线程组，等待完成");
        CheckTaskPool.getInstance().shutdown();
        logger.info("7、callback完成");
        System.exit(0);
    }


    private static void startNewProcess() throws Exception {
        String log = " log-" + System.currentTimeMillis() + ".txt";
        ShellUtil.shellExecute(ConfigParams.shellPathR + log, false);
    }
}
