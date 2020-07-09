package com.ponder.m3u8java.base;

import com.ponder.m3u8java.downloader.DownloadFactory;
import com.ponder.m3u8java.downloader.Downloader;
import com.ponder.m3u8java.util.FileUtil;
import com.ponder.m3u8java.util.Log;

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
    public Map<String,String> headers = new HashMap<String, String>();
    public List<String> subM3u8s = new ArrayList<String>();
    private List<TS> body = new ArrayList<TS>();
    private final String baseDir = "D:/ts/";
    private final String cacheDir = baseDir+"cache/";
    private String aesKey;
    private Downloader downloader = DownloadFactory.getDownloader(DownloadFactory.Type.URL_CONNECTION);

    public M3u8(String url) throws IOException {
        this.host = url.substring(0,url.lastIndexOf("/")+1);
        this.path = url.substring(url.lastIndexOf("/")+1);
        this.name = path.replace(".m3u8","");
        this.inputStream = downloader.getStream(url);
        init();
    }

    public M3u8(String host,String path) throws IOException {
        this.host = host;
        this.path = path;
        this.name = path.replace(".m3u8","");
        this.inputStream = downloader.getStream(host+path);
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
            //解析m3u8
            Parser.parse(inputStream,this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.log("解析m3u8失败");
        }
    }

    public void addTS(float duration, String url){
        body.add(new TS(duration,url));
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
            byte[] bodyBytes = downloader.getBytes(host + ts);
            File tsFile = new File(cacheDir +name+"/"+ts.substring(ts.lastIndexOf("/")+1));
            FileUtil.writeBodyBytesToFile(bodyBytes,tsFile);
            tsObj.setTsFile(tsFile.toString());
            Log.log("下载第"+i+"个"+ts);
        }
        downloadCallback.onComplete(body);
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
            downloader.addTSDownloadTask(body.get(i),tsDownloadCallback);
        }
    }

    /**
     * 获取AES key
     * @return
     * @throws IOException
     */
    public String getKey() throws IOException {
        Map<String, String> keys = Parser.parseExtKey(this);
        if (keys==null)return null;
        if (keys.get("METHOD").equalsIgnoreCase("AES-128")){
            String keyUrl = keys.get("URI");
            aesKey = downloader.getString(host + keyUrl);
        }
        return aesKey;
    }



    public class TS{
        private float duration;
        private String url;
        private String tsFile;
        private boolean downloaded;

        public TS(float duration, String url) {
            this.duration = duration;
            this.url = url;
        }

        public String getHost(){
            return host;
        }

        public String getCacheFolder(){
            return cacheDir+name+"/";
        }

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

    Downloader.TsDownloadCallback tsDownloadCallback = new Downloader.TsDownloadCallback() {
        @Override
        public void onTsDownloaded(TS ts) throws IOException {
            boolean isAllDownload=true;
            for (TS t:body){
                if (!t.isDownloaded())isAllDownload=false;
            }
            if (isAllDownload) downloadCallback.onComplete(body);
        }
    };

    DownloadCallback downloadCallback = new DownloadCallback() {
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
            Log.log("合并完成");
        }
    };

    interface DownloadCallback {
        void onComplete(List<TS> tsList) throws IOException;
    }

}
