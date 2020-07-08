package com.ponder.m3u8java;

import java.io.*;

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

    private void writeStreamToFile(InputStream is, File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        byte[] buffer = new byte[1024];
        int l;
        while ((l = is.available()) != 0){
            is.read(buffer);
            tsOutputStream.write(buffer,0,l);
        }
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
