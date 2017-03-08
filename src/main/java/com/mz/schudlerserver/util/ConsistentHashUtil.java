package com.mz.schudlerserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import com.mz.schudlerserver.common.ConfigParams;

public class ConsistentHashUtil {  
	   
       private final HashFunction hashFunction = new HashFunction(); //Hash计算对象，用于自定义hash算法 
       private int numberOfReplicas;// 复制的节点个数
       //一致性Hash环
       private final SortedMap<Integer, String> circle = new TreeMap<Integer, String>(); 
       private static ConsistentHashUtil instance = null;
       private ReentrantLock reentrantLock = new ReentrantLock();
  
       private ConsistentHashUtil() {  
          
       } 
       
       public static ConsistentHashUtil getInstance(){
           if(instance == null){
               synchronized (ConsistentHashUtil.class){
                   if(instance == null){
                       instance = new ConsistentHashUtil();
                   }
               }
           }
           return instance;
       }
       
       /**
        * 节点初始化
        * @param numberOfReplicas 复制的节点个数
        * @param nodes 节点
        */
      public void init(int numberOfReplicas, String[] nodes){
           this.numberOfReplicas = numberOfReplicas;  
           //初始化节点
           for (String node : nodes) {  
                add(node);  
          }  
      }
      
      /**
       * 批量添加节点
       * @param numberOfReplicas 复制的节点个数
       * @param nodes 节点
       */
     public void add(String[] nodes){
          for (String node : nodes) {  
               add(node);  
         }  
     }
      
       
       
      /**
       * 增加节点
       * 每增加一个节点，就会在闭环上增加给定复制节点数
       * 例如复制节点数是2，则每调用此方法一次，增加两个虚拟节点，这两个节点指向同一Node
       * 由于hash算法会调用node的toString方法，故按照toString去重
       * @param node 节点对象
       */
       public void add(String node) { 
    	     reentrantLock.lock();
             for (int i = 0; i < numberOfReplicas; i++) {  
                   circle.put(hashFunction .hash(node.toString() + i), node); 
             }
             reentrantLock.unlock();
      }  
  
       /**
        * 移除节点的同时移除相应的虚拟节点
        * @param node 节点对象
        */
       public void remove(String node) { 
    	     reentrantLock.lock();
             for (int i = 0; i < numberOfReplicas; i++) { 
                   circle.remove(hashFunction .hash(node.toString() + i)); 
             }
             reentrantLock.unlock();
      }  
  
       /**
        * 获得一个最近的顺时针节点
        * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
        * @return 节点对象
        */
       public String get(Object key) {  
             if (circle.isEmpty()) {  
                   return null ;  
            }  
             int hash = hashFunction .hash(key);  
             // System.out.println("hash---: " + hash);  
             if (!circle.containsKey(hash)) {  
                  SortedMap<Integer, String> tailMap = circle.tailMap(hash);  
                  hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();  
            }  
             // System.out.println("hash---: " + hash);  
             return circle.get(hash);  
      }  
  
       /**
        * Hash算法对象，用于自定义hash算法
        * @author Yuanbin LIN
        * @date 2016年12月14日
        * @version $Revision$
        */
       static class HashFunction {  
             int hash(Object key) {
                   //md5加密后，hashcode
                   return Md5Encrypt.md5(key.toString()).hashCode();  
            }  
      }  
  
       public static void main(String [] args) {  
            String[] nodes = {"A","B","C","D"};
            
  
            Map< Object, Integer> map = new HashMap< Object, Integer>();  
  
            ConsistentHashUtil consistentHash = ConsistentHashUtil.getInstance();
            consistentHash.init(100, nodes);
             List<String> list = new ArrayList<>();
             int count = 10000;
             for(int i = 0; i < count ; i++){
            	   list.add("1111-2222"+i);
                   list.add("1111-2222"+i);
                   list.add("2222-2222"+i);
                   list.add("3333-4444"+i);
                   list.add("5555-6666"+i);
             }
          
              
  
             for (int i = 0; i < list.size(); i++) {  
            	  Object key = consistentHash.get(list.get(i));  
                   if (map.containsKey(key)) {  
                        map.put(consistentHash.get(list.get(i)), map.get(key) + 1);  
                  } else {  
                        map.put(consistentHash.get(list.get(i)), 1);  
                  }  
                   // System.out.println(key);  
            }  
  
             showServer(map);  
            map.clear();  
            consistentHash.remove( "A" );  
  
            System. out .println("------- remove A" );  
  
             for (int i = 0; i < list.size(); i++) {  
            	 Object key = consistentHash.get(list.get(i));  
                   if (map.containsKey(key)) {  
                        map.put(consistentHash.get(list.get(i)), map.get(key) + 1);  
                  } else {  
                        map.put(consistentHash.get(list.get(i)), 1);  
                  }  
                   // System.out.println(key);  
            }  
  
             showServer(map);  
            map.clear();  
            consistentHash.add( "E" );  
            System. out .println("------- add E" );  
  
             for (int i = 0; i < list.size(); i++) {  
            	 Object key = consistentHash.get(list.get(i));  
                   if (map.containsKey(key)) {  
                        map.put(consistentHash.get(list.get(i)), map.get(key) + 1);  
                  } else {  
                        map.put(consistentHash.get(list.get(i)), 1);  
                  }  
                   // System.out.println(key);  
            }  
  
             showServer(map);  
            map.clear();  
  
            consistentHash.add( "F" );  
            System. out .println("------- add F服务器  业务量加倍" );  
            count = count * 2;  
             for (int i = 0; i < list.size(); i++) {  
            	 Object key = consistentHash.get(list.get(i));  
                   if (map.containsKey(key)) {  
                        map.put(consistentHash.get(list.get(i)), map.get(key) + 1);  
                  } else {  
                        map.put(consistentHash.get(list.get(i)), 1);  
                  }  
                   // System.out.println(key);
               
            }  
  
             showServer(map);  
             
             map.clear();  
             consistentHash.remove( "B" );  
             consistentHash.remove( "C" );  
             consistentHash.remove( "D" );  
             consistentHash.remove( "E" );  
             consistentHash.remove( "F" ); 
             consistentHash.add(ConfigParams.haproxyAddrs);
             
             System. out .println("------- 测试haproxy服务器" );  
   
              for (int i = 0; i < list.size(); i++) {  
             	 Object key = consistentHash.get(list.get(i));  
                    if (map.containsKey(key)) {  
                         map.put(consistentHash.get(list.get(i)), map.get(key) + 1);  
                   } else {  
                         map.put(consistentHash.get(list.get(i)), 1);  
                   }  
             }  
   
              showServer(map);  
             
            
  
      }  
  
       public static void showServer(Map<Object , Integer> map) {  
             for (Entry<Object, Integer> m : map.entrySet()) {  
                  System. out .println("服务器 " + m.getKey() + "----" + m.getValue() + "个" );  
            }  
      }  
  
}  

