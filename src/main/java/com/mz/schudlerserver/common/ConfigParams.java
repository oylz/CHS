package com.mz.schudlerserver.common;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mz.schudlerserver.netty.connhandler.HolderConnHandler;


public class ConfigParams {
	private static final Logger logger = LoggerFactory.getLogger(ConfigParams.class);
 
    public static String[] haproxyAddrs = new String[]{};
    public static int numberOfReplicas;
    public static int checkHealthTimeInterval;
    public static String checkHealthRunning;
    public static String timeRequestStr;
    public static String systemaddrRequestStr;
    public static int notHealthTotalRetryTimes;
    public static int notHealthTimeInterval;
    public static int corePoolSize;
    public static int maximumPoolSize;
    public static int keepAliveTime;
    public static int workQueueCapacity;
    public static String killSignal;
    public static String shellPathSt1;
    public static String shellPathR;
    public static String shellPathSt2;
    public static int requestTimestampLimit;
    public static String authenticationKey;
    public static String blowfishECBKey;
    
    public static int answer_corePoolSize;
    public static int answer_maximumPoolSize;
    
	private static void print(String key, Object value){
		logger.info(key + "-------" + value);
	}

    static {
        InputStream is = ConfigParams.class.getClassLoader().getResourceAsStream("config.properties");
        Properties p = new Properties();
        try {
            p.load(is);
            haproxyAddrs = p.getProperty("haproxy.addrs","").split(",");
            print("haproxyAddrs", p.getProperty("haproxy.addrs", ""));
            
            numberOfReplicas = Integer.valueOf(p.getProperty("haproxy.numberOfReplicas", "10"));
            print("numberOfReplicas", numberOfReplicas);
            
            checkHealthTimeInterval = Integer.valueOf(p.getProperty("haproxy.checkHealth.time.interval", "1000")) * 1000;
            print("checkHealthTimeInterval", checkHealthTimeInterval);
            
            checkHealthRunning = p.getProperty("haproxy.checkHealth.running","yes");
            print("checkHealthRunning", checkHealthRunning);

            timeRequestStr = p.getProperty("time.request.str","/time");
            print("timeRequestStr", timeRequestStr);

            systemaddrRequestStr = p.getProperty("systemaddr.request.str","/systemaddr");
            print("systemaddrRequestStr", systemaddrRequestStr);
            
            notHealthTotalRetryTimes = Integer.valueOf(p.getProperty("haproxy.notHealth.totalRetryTimes", "1"));
            print("notHealthTotalRetryTimes", notHealthTotalRetryTimes);
            
            notHealthTimeInterval = Integer.valueOf(p.getProperty("haproxy.notHealth.time.interval", "500"));
            print("notHealthTimeInterval", notHealthTimeInterval);
            
            corePoolSize = Integer.valueOf(p.getProperty("checkPool.corePoolSize", "3"));
            print("corePoolSize", corePoolSize);
            
            maximumPoolSize = Integer.valueOf(p.getProperty("checkPool.maximumPoolSize", "3"));
            print("maximumPoolSize", maximumPoolSize);
            
            keepAliveTime = Integer.valueOf(p.getProperty("checkPool.keepAliveTime", "20"));
            print("keepAliveTime", keepAliveTime);
            
            workQueueCapacity = Integer.valueOf(p.getProperty("checkPool.workQueueCapacity", "200"));
            print("workQueueCapacity", workQueueCapacity);

            killSignal = p.getProperty("kill.dev.signal", "TERM");
            print("killSignal", killSignal);

            shellPathSt1 = p.getProperty("shell.path.st1", "/var/local/ss/st1.sh");
            print("shellPathSt1", shellPathSt1);

            shellPathR = p.getProperty("shell.path.r", "/var/local/ss/r.sh");
            print("shellPathR", shellPathR);

            shellPathSt2 = p.getProperty("shell.path.st2", "/var/local/ss/st2.sh");
            print("shellPathSt2", shellPathSt2);

            requestTimestampLimit = Integer.valueOf(p.getProperty("request.timestamp.limit", "5"));
            print("requestTimestampLimit", requestTimestampLimit);

            authenticationKey = p.getProperty("authentication.key", "#$TQdg4WT#R!@df1");
            print("authenticationKey", authenticationKey);

            blowfishECBKey = p.getProperty("blowfishECB.key", "adst4W#gasdGq446");
            print("blowfishECBKey", blowfishECBKey);
            
            answer_corePoolSize = Integer.valueOf(p.getProperty("answerPool.corePoolSize", "3"));
            print("answer_corePoolSize", answer_corePoolSize);
            
            answer_maximumPoolSize = Integer.valueOf(p.getProperty("answerPool.maximumPoolSize", "3"));
            print("answer_maximumPoolSize", answer_maximumPoolSize);
            
        } catch (Exception e) {
            logger.error("不能读取属性文件. " + "请确保config.properties在CLASSPATH指定的路径中");
        }
    }
    
    
   
    
}
