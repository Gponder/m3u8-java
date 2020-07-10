package com.ponder.m3u8java.util;

import java.io.*;
import java.lang.reflect.Field;

public class FileUtil {

    /**
     * 存储视频分片
     * @param bodyBytes
     * @param tsFile
     * @throws IOException
     */
    public static void writeBodyBytesToFile(byte[] bodyBytes, File tsFile) throws IOException {
        File tsParentFile = new File(tsFile.getParent());
        if (!tsParentFile.exists())tsParentFile.mkdirs();
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        tsOutputStream.write(bodyBytes);
        tsOutputStream.flush();
        tsOutputStream.close();
    }

    /**
     * 读取文件到byte数组
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromFile(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        return buffer;
    }

    /**
     * 合并文件工具
     * @param filePath
     * @throws IOException
     */
    public static void mergeFile(String filePath) throws IOException {
        File file=new File(filePath);
        String[] files = file.list();
        FileOutputStream fos = new FileOutputStream(filePath + "/merged");
        for (String ts:files){
            fos.write(readBytesFromFile(ts));
        }
        fos.flush();
        fos.close();
    }

    /**
     * 因为java utf-8 解码 编码 非码区数据无法还原所以不能通过字符串编码还原数据
     * @param bodyString
     * @param tsFile
     * @throws IOException
     */
    private void writeBodyStringToFile(String bodyString,File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        tsOutputStream.write(bodyString.getBytes("UTF-8"));
        tsOutputStream.flush();
        tsOutputStream.close();
    }

    public static String writeStreamToFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1){
            fos.write(buffer,0,len);
        }
        fos.flush();
        fos.close();
        return file.getPath();
    }

    /**
     * 读取文件
     * @param file
     * @return
     * @throws IOException
     */
    public byte[] readFile(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        return buffer;
    }

    /**
     * byte数组写入到文件
     * @param bytes
     * @param file
     * @throws IOException
     */
    public void writeFile(byte[] bytes,String file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }

}
