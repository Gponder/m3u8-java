package com.ponder.m3u8java;

import java.io.*;
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

    private String host;
    private String path;
    private String name;
    private InputStream inputStream;
    private Map<String,String> headers = new HashMap<String, String>();
    private List<String> subM3u8s = new ArrayList<String>();
    private List<TS> body = new ArrayList<TS>();
    private List<String> tsFiles = new ArrayList<String>();
    private final String baseDir = "D:/ts/";
    private final String cacheDir = baseDir+"cache/";
    private String aesKey;

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
    //视频分片标志
    public final static String EXT_INF = "#EXTINF:";
    //结束 #EXT-X-ENDLIST
    public final static String EXT_X_ENDLIST = "#EXT-X-ENDLIST";

    public M3u8(String url) throws Exception {
        this.host = url.substring(0,url.lastIndexOf("/")+1);
        this.path = url.substring(url.lastIndexOf("/")+1);
        this.name = path.replace(".m3u8","");
        this.inputStream = new Downloader(url).download();
        init();
    }

    public M3u8(String host,String path) throws IOException {
        this.host = host;
        this.path = path;
        this.name = path.replace(".m3u8","");
        this.inputStream = new Downloader(host+path).download();
        init();
    }

    public M3u8(File file,String host) throws FileNotFoundException {
        this.host = host;
        this.path = file.getPath().substring(file.getPath().lastIndexOf("/"));
        this.name = path.replace(".m3u8","");
        this.inputStream = new FileInputStream(file);
        init();
    }

    public M3u8(InputStream inputStream,String host) {
        this.host = host;
        this.name = String.valueOf(System.currentTimeMillis());
        this.inputStream = inputStream;
        init();
    }

    private void init() {
        try {
            parse();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("解析m3u8失败");
        }
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
            if (line.startsWith(EXT_INF)){
                TS ts = new TS();
                ts.setDuration(Float.parseFloat(line.substring(EXT_INF.length(),line.indexOf(","))));
                line = reader.readLine();
                if (line.endsWith(".ts")){
                    ts.setUrl(line);
                }
                body.add(ts);
            }
            if(line.startsWith(EXT_X_ENDLIST)){
                break;
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

    public void download() throws IOException {
        if (hasSubM3u8()){
            downloadSubM3u8().download();
        }else{
            downloadBodiesWithPool();
        }
    }

    public M3u8 downloadSubM3u8() throws IOException {
        String url = subM3u8s.get(0);
        String subHost = host+url.substring(0,url.lastIndexOf("/")+1);
        String subPath = url.substring(url.lastIndexOf("/") + 1);
        return new M3u8(subHost,subPath);
    }

    /**
     * 下载视频分片
     * @return
     * @throws IOException
     */
    public boolean downloadBodies() throws IOException {
        if (body.size()==0)return false;
        for (int i=0;i<body.size();i++){
            TS tsObj = body.get(i);
            String ts = tsObj.getUrl();
            byte[] bodyBytes = new Downloader(host + ts).getBodyBytes();
            File tsFile = new File(cacheDir +name+"/"+ts.substring(ts.lastIndexOf("/")+1));
            FileUtil.writeBodyBytesToFile(bodyBytes,tsFile);
            tsObj.setTsFile(tsFile.toString());
            System.out.println("下载第"+i+"个"+ts);
        }
        downloadComplete.onComplete(body);
        return true;
    }

    /**
     * 下载视频分片
     * @return
     * @throws IOException
     */
    public void downloadBodiesWithPool() throws IOException {
        if (body.size()==0)return;
        for (int i=0;i<body.size();i++){
            new DownLoadRunnable(body.get(i), this, tsDownloadComplete).download();
        }
    }

    /**
     * 获取AES key
     * @return
     * @throws IOException
     */
    public String getKey() throws IOException {
        Map<String, String> keys = parseExtKey();
        if (keys.get("METHOD").equalsIgnoreCase("AES-128")){
            String keyUrl = keys.get("URI");
            aesKey = new Downloader(host + keyUrl).getBodyString();
        }
        return aesKey;
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

    public String getHost() {
        return host;
    }

    public String getTsCacheFolder() {
        return cacheDir+name+"/";
    }

    class TS{
        private float duration;
        private String url;
        private String tsFile;
        private boolean downloaded;

        public float getDuration() {
            return duration;
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTsFile() {
            return tsFile;
        }

        public void setTsFile(String tsFile) {
            this.tsFile = tsFile;
        }

        public boolean isDownloaded() {
            return downloaded;
        }

        public void setDownloaded(boolean downloaded) {
            this.downloaded = downloaded;
        }
    }

    DownLoadRunnable.TsDownloadComplete tsDownloadComplete = new DownLoadRunnable.TsDownloadComplete() {
        @Override
        public void onTsDownloaded(TS ts) throws IOException {
            boolean isAllDownload=true;
            for (TS t:body){
                if (!t.isDownloaded())isAllDownload=false;
            }
            if (isAllDownload)downloadComplete.onComplete(body);
        }
    };

    DownloadComplete downloadComplete = new DownloadComplete() {
        @Override
        public void onComplete(List<TS> tsList) throws IOException {
            FileOutputStream fos = new FileOutputStream(baseDir + name);
            for (TS ts:tsList){
                FileInputStream fis = new FileInputStream(ts.getTsFile());
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                fos.write(buffer);
            }
            fos.flush();
            fos.close();
            System.out.println("合并完成");
        }
    };

    interface DownloadComplete {
        void onComplete(List<TS> tsList) throws IOException;
    }

}
