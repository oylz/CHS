package com.mz.schudlerserver.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mz.schudlerserver.common.ConfigParams;
import com.mz.schudlerserver.common.GlobalParams;
import com.mz.schudlerserver.model.HaproxyModel;


public class HaProxyAddrsUtil {
	 private static HaProxyAddrsUtil instance = null;
	 //key为：haproxy的ip+端口
	 public  Map<String, HaproxyModel> haproxyMap = new ConcurrentHashMap<>();
	 
	  private HaProxyAddrsUtil() {  
          
      } 
      
      public static HaProxyAddrsUtil getInstance(){
          if(instance == null){
              synchronized (HaProxyAddrsUtil.class){
                  if(instance == null){
                      instance = new HaProxyAddrsUtil();
                      instance.initHaproxyAddrs();
                  }
              }
          }
          return instance;
      }
      
      
    private void initHaproxyAddrs(){
    	String[] haproxyAddrs = ConfigParams.haproxyAddrs;
		for(String addr : haproxyAddrs){
			if(addr.contains(GlobalParams.HAPROXY_DOMAIN_IP_SPLIT)){
                String[] domainIps = addr.split(GlobalParams.HAPROXY_DOMAIN_IP_SPLIT);
                if(domainIps != null && domainIps.length == 2){
                    String ipPort = domainIps[1];
                    if(ipPort.contains(GlobalParams.HAPROXY_IP_PORT_SPLIT)){
                        String[] haproxy = ipPort.split(GlobalParams.HAPROXY_IP_PORT_SPLIT);
                        if(haproxy != null && haproxy.length == 2){
                            String ip = haproxy[0];
                            String port = haproxy[1];
                            HaproxyModel model = new HaproxyModel();
                            model.setIp(ip);
                            model.setPort(port);
                            model.setHostName(domainIps[0]);
                            haproxyMap.put(addr, model);
                        }
                    }
                }
            }
		}
    }
      
  
}
