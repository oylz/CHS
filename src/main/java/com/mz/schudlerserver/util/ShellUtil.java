package com.mz.schudlerserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ShellUtil {
    /**
     * 执行脚本
     * @param path
     * @param isSync
     */
    public static String shellExecute(String path, boolean isSync) throws Exception {
        String line = "";
        BufferedReader br;
        Process p = Runtime.getRuntime().exec(path);
        if (isSync) {
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
            }
            p.waitFor();
            br.close();
        }
        return line;
    }
}
