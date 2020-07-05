package com.ponder.m3u8java;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 18:04
 */
public class M3u8 {

    private String url;
    private String host;
    private InputStream inputStream;
    private Map<String,String> headers = new HashMap<String, String>();
    private List<String> subM3u8s = new ArrayList<String>();
    private List<String> body = new ArrayList<String>();
    private List<String> tsFiles = new ArrayList<String>();
    private String cacheDir = "D:/ts/cache/";

    //headers
    //视频流信息 #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1665000,RESOLUTION=960x540
    public final static String EXT_X_STREAM_INF = "#EXT-X-STREAM-INF:";
    //版本号 #EXT-X-VERSION:3
    public final static String EXT_X_VERSION = "#EXT-X-VERSION:";
    //媒体时长最大值 只有有视频信息的m3u8才有此标志 #EXT-X-TARGETDURATION:9
    public final static String EXT_X_TARGETDURATION = "#EXT-X-TARGETDURATION:";
    //:<EVENT|VOD> VOD服务器不能改变PlayList文件用于视频，EVENT服务器可以向该文件中增加新的一行内容用于直播  #EXT-X-PLAYLIST-TYPE:VOD
    public final static String EXT_X_PLAYLIST_TYPE = "#EXT-X-PLAYLIST-TYPE:";
    // 每一个media URI 在 PlayList中只有唯一的序号，相邻之间序号+1 #EXT-X-MEDIA-SEQUENCE:0
    public final static String EXT_X_MEDIA_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE:";
    //解码NONE 或者 AES-128。如果是NONE，则URI以及IV属性必须不存在，如果是AES-128(Advanced Encryption Standard)，则URI必须存在，IV可以不存在。
    //#EXT-X-KEY:METHOD=AES-128,URI="/key.key"
    public final static String EXT_X_KEY = "#EXT-X-KEY:";

    public M3u8(String url) throws Exception {
        this(new Downloader(url).download(),new URI(url).getHost());
    }

    public M3u8(String file,String host) throws FileNotFoundException {
        this(new FileInputStream(file),host);
    }

    public M3u8(InputStream inputStream,String host) {
        this.inputStream = inputStream;
        this.host = host;
    }

    /**
     * 解析m3u8
     * @return
     * @throws IOException
     */
    public M3u8 parse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int counter=0;
        while ((line = reader.readLine())!=null) {
            //校验格式
            if (counter==0 && !line.startsWith("#EXTM3U")){
                throw new RuntimeException("资源不是 m3u8 格式");
            }
            //解析协议头
            if (line.contains(EXT_X_STREAM_INF)){
                headers.put(EXT_X_STREAM_INF,line);
            }
            if (line.contains(EXT_X_VERSION)){
                headers.put(EXT_X_VERSION,line);
            }
            if (line.contains(EXT_X_TARGETDURATION)){
                headers.put(EXT_X_TARGETDURATION,line);
            }
            if (line.contains(EXT_X_PLAYLIST_TYPE)){
                headers.put(EXT_X_PLAYLIST_TYPE,line);
            }
            if (line.contains(EXT_X_MEDIA_SEQUENCE)){
                headers.put(EXT_X_MEDIA_SEQUENCE,line);
            }
            if (line.contains(EXT_X_KEY)){
                headers.put(EXT_X_KEY,line);
            }
            if (line.endsWith(".ts")){
                body.add(line);
            }
            //m3u8包涵的m3u8
            if (line.endsWith(".m3u8")){
                subM3u8s.add(line);
            }
            counter++;
        }
        return this;
    }

    public boolean hasSubM3u8(){
        return subM3u8s.size()!=0;
    }

    /**
     * 下载视频分片
     * @return
     * @throws IOException
     */
    public boolean downloadBodies() throws IOException {
        if (body.size()==0)return false;
        if (!new File(cacheDir).exists())new File(cacheDir).mkdirs();
        for (int i=0;i<body.size();i++){
            String ts = body.get(i);
            String bodyString = new Downloader(host + ts).getBodyString();
            String name = generateFileName(ts);
            File tsFile = new File(cacheDir + name);
            writeBodyStringToFile(bodyString,tsFile);
            System.out.println("下载第"+i+"个"+ts);
        }
        return true;
    }

    private String generateFileName(String ts) {
        return ts.substring(ts.lastIndexOf("/")+1,ts.lastIndexOf("."));
    }

    /**
     * 存储视频分片
     * @param bodyString
     * @param tsFile
     * @throws IOException
     */
    private void writeBodyStringToFile(String bodyString,File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        tsOutputStream.write(bodyString.getBytes("UTF-8"));
        tsOutputStream.flush();
        tsOutputStream.close();
        tsFiles.add(tsFile.toString());
    }

    private void writeToCacheFile(InputStream is, File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        byte[] buffer = new byte[1024];
        int l;
        while ((l = is.available()) != 0){
            is.read(buffer);
            tsOutputStream.write(buffer,0,l);
        }
        tsFiles.add(tsFile.toString());
    }

    /**
     * 获取AES key
     * @return
     * @throws IOException
     */
    public String getKey() throws IOException {
        Map<String, String> keys = parseExtKey();
        String key = null;
        if (keys.get("METHOD").equalsIgnoreCase("AES-128")){
            String keyUrl = keys.get("URI");
            key = new Downloader(host + keyUrl).getBodyString();
        }
        return key;
    }

    /**
     * 解析AES key地址
     * @return
     */
    private Map<String, String> parseExtKey() {
        Map<String,String> extKey = null;
        String key = headers.get(EXT_X_KEY);
        if (key!=null){
            extKey = new HashMap<String, String>();
            String[] values = key.split(":")[1].split(",");
            for (String kv:values){
                String[] kvArr = kv.split("=");
                extKey.put(kvArr[0],kvArr[1].replace("\"",""));
            }
        }
        return extKey;
    }

}
