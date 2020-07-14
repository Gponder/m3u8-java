package com.ponder.m3u8java.config;

import com.ponder.m3u8java.base.M3u8;
import org.apache.http.util.TextUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/14 15:10
 */
public class Config {

    private static String dirK = "baseDir";
    private static String configDir = "config";
    private static File configFile = new File(configDir+"/Config.properties");

    public static void init() {
        if (configFile.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
                String line;
                while ((line=bufferedReader.readLine())!=null) {
                    if (!TextUtils.isEmpty(line)){
                        String[] kv = line.split("=");
                        if (dirK.equalsIgnoreCase(kv[0]) &&
                                !TextUtils.isEmpty(kv[1].trim()) &&
                                kv[1].endsWith("/")){
                            M3u8.baseDir=kv[1].trim();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            new File(configDir).mkdirs();
        }
    }

    public static void setBaseDir(String baseDir){
        if (TextUtils.isEmpty(baseDir)||!baseDir.endsWith("/"))return;
        try {
            ArrayList<String> lines = new ArrayList<>();
            boolean hasConfigBaseDir = false;
            if (configFile.exists()){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
                String line;
                while ((line=bufferedReader.readLine())!=null) {
                    lines.add(line);
                    if (line.startsWith(dirK)){
                        line.replace(line.substring(line.indexOf("=")+1),baseDir);
                        hasConfigBaseDir=true;
                    }
                }
                bufferedReader.close();
            }
            if (!hasConfigBaseDir){
                lines.add(dirK+"="+baseDir);
            }
            M3u8.baseDir = baseDir;
            if (!new File(configDir).exists())new File(configDir).mkdirs();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)));
            for (String wLine:lines){
                bufferedWriter.write(wLine);
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setBaseDir("d:/ts/test/");
    }
}
