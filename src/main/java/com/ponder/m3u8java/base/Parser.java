package com.ponder.m3u8java.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    public static void parse(InputStream inputStream,M3u8 m3u8) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int counter=0;
        while ((line = reader.readLine())!=null) {
            //校验格式
            if (counter==0 && !line.startsWith("#EXTM3U")){
                throw new RuntimeException("资源不是 m3u8 格式");
            }
            //解析协议头
            parseHead(m3u8.headers, line);
            //解析视频分片
            if (line.startsWith(HeadMark.EXT_INF.toString())){
                float duration = Float.parseFloat(line.substring(HeadMark.EXT_INF.toString().length(), line.indexOf(",")));
                line = reader.readLine();
                if (!line.endsWith(".ts"))throw new RuntimeException("解析视频分片失败");
                m3u8.addTS(duration,line);
            }
            //解析视频流(重定向的m3u8)  m3u8包涵的m3u8
            if (line.startsWith(HeadMark.EXT_X_STREAM_INF.toString())){
                Map<String,String> mapInfo = parseHeadToMap(line);
                String url = reader.readLine();
                if(!url.endsWith(".m3u8"))throw new RuntimeException("重定向视频流解析失败");
                m3u8.addSubM3u8(url,line,mapInfo);
            }
            if(line.startsWith(HeadMark.EXT_X_ENDLIST.toString())){
                break;
            }
            counter++;
        }
    }

    private static void parseHead(Map<String, String> headers, String line) {
        for (HeadMark mark:HeadMark.values()){
            if (line.contains(mark.toString())){
                headers.put(mark.toString(),line);
            }
        }
    }

    /**
     * 解析AES key地址
     * @return  String key = m3u8.headers.get(HeadMark.EXT_X_KEY.toString());
     */
    public static Map<String, String> parseHeadToMap(String line) {
        Map<String,String> extKey = null;
        if (line!=null){
            extKey = new HashMap<String, String>();
            String[] values = line.split(":")[1].split(",");
            for (String kv:values){
                String[] kvArr = kv.split("=");
                extKey.put(kvArr[0],kvArr[1].replace("\"",""));
            }
        }
        return extKey;
    }

    /**
     * 解析新的 host
     * @param host
     * @param path
     * @param subPath
     * @return
     */
    public static String assembleHost(String host, String path, String subPath){
        if (subPath.startsWith("/")){
            return host;
        }else{
            String url = host+path;
            return url.substring(0,url.lastIndexOf("/")+1);
        }
    }

    //headers
    enum HeadMark{
        EXT_X_STREAM_INF("#EXT-X-STREAM-INF:"),             //视频流信息     #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1665000,RESOLUTION=960x540
        EXT_X_VERSION("#EXT-X-VERSION:"),                   //版本号         #EXT-X-VERSION:3
        EXT_X_TARGETDURATION("#EXT-X-TARGETDURATION:"),     //媒体时长最大值 只有有视频信息的m3u8才有此标志     #EXT-X-TARGETDURATION:9
        EXT_X_PLAYLIST_TYPE("#EXT-X-PLAYLIST-TYPE:"),       //:<EVENT|VOD> VOD服务器不能改变PlayList文件用于视频，EVENT服务器可以向该文件中增加新的一行内容用于直播  #EXT-X-PLAYLIST-TYPE:VOD
        EXT_X_MEDIA_SEQUENCE("#EXT-X-MEDIA-SEQUENCE:"),     // 每一个media URI 在 PlayList中只有唯一的序号，相邻之间序号+1 #EXT-X-MEDIA-SEQUENCE:0
        EXT_X_KEY("#EXT-X-KEY:"),                           //解码NONE 或者 AES-128     #EXT-X-KEY:METHOD=AES-128,URI="/key.key"
        EXT_INF("#EXTINF:"),                                //视频分片标志
        EXT_X_ENDLIST("#EXT-X-ENDLIST"),                    //结束 #EXT-X-ENDLIST
        ;

        private String mark;

        HeadMark(String mark) {
            this.mark = mark;
        }

        @Override
        public String toString() {
            return mark;
        }
    }

}
