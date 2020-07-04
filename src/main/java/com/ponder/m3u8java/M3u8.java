package com.ponder.m3u8java;

import javax.jnlp.ClipboardService;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void parse() throws IOException {
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
            if (line.endsWith(".ts"))
            //m3u8包涵的m3u8
            if (line.endsWith(".m3u8")){
                subM3u8s.add(line);
            }
            counter++;
        }
    }

}
