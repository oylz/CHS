package com.mz.schudlerserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;


public class NettyConfig {

    private static Logger logger = LoggerFactory.getLogger(NettyConfig.class);


    protected static Properties prop = new Properties();
    public static final String CFG_FILE = "netty.properties";
    public static boolean isInited = false;


    public static synchronized void init() {
        isInited = true;
        logger.info("-----------init NmsConfig begin---------");
        reload();
        logger.info("-----------init NmsConfig end---------");
    }

    private synchronized static void reload() {
        InputStream inputStream = NettyConfig.class.getClassLoader()
                .getResourceAsStream(CFG_FILE);
        if (inputStream == null) {
            logger.error("cannot load " + CFG_FILE);
        }
        try {
            prop.load(inputStream);
            logger.info("load " + CFG_FILE + " ok.");
        } catch (IOException e) {
            logger.error("load " + CFG_FILE + " error!" + e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("" + e);
            }
        }
    }

    public static synchronized String getPropValue(String key) {
        return getPropValue(key, null);
    }

    public static synchronized String getPropValue(String key, String defaultValue) {
        if (!isInited) {
            init();
        }
        if (key == null) {
            return null;
        }

        return prop.getProperty(key, defaultValue);
    }

    public static synchronized void setProValue(String key, String value){
        if (!isInited) {  //判断是否有进行初始化
            init();
        }
        if (key == null) {
            return;
        }

        OutputStream os = null;
        try {
            URL filePath=NettyConfig.class.getResource("/");
            os =new FileOutputStream(filePath.getPath()+"/log_size_config.properties");
            prop.setProperty(key, value);
            prop.store(os, "author:Liang");
        } catch (IOException e) {
            // TODO: handle exception
            logger.error("" + e.getMessage(),e);
        }finally {
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("" + e.getMessage(),e);
                }
            }
        }
    }

}
