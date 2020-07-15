package com.ponder.m3u8java.base;

import com.ponder.m3u8java.aes.AesUtil;
import com.ponder.m3u8java.downloader.DownloadFactory;
import com.ponder.m3u8java.downloader.Downloader;
import com.ponder.m3u8java.util.FileUtil;
import com.ponder.m3u8java.util.Log;

import java.io.*;
import java.net.URL;
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
    public List<SubInfo> subM3u8s = new ArrayList<SubInfo>();
    private List<TS> body = new ArrayList<TS>();
    public static String baseDir = "E:/ts/";
    private final String cacheDir = baseDir+"cache/";
    private String aesKey;
    private Downloader downloader = DownloadFactory.getDownloader(DownloadFactory.Type.URL_CONNECTION);
    private DownloadStateCallback downloadStateCallback;
    private DownloadState downloadState = DownloadState.Init;

    public M3u8(String url) throws IOException {
        String h = new URL(url).getHost();
        this.host = url.substring(0,url.indexOf(h)+h.length());
        this.path = url.replace(host,"");
        this.name = path.replace(".m3u8","").replace("/","");
        this.inputStream = downloader.getStream(url);
        init();
    }

    public M3u8(String host,String path) throws IOException {
        this.host = host;
        this.path = path;
        this.name = path.replace(".m3u8","").replace("/","");
        this.inputStream = downloader.getStream(host+path);
        init();
    }

    public M3u8(File file,String host) throws FileNotFoundException {
        this.host = host;
        this.path = file.getPath().substring(file.getPath().lastIndexOf("/"));
        this.name = path.replace(".m3u8","").replace("/","");
        this.inputStream = new FileInputStream(file);
        init();
    }

    public M3u8(InputStream inputStream,String host) {
        this.host = host;
        this.name = String.valueOf(System.currentTimeMillis());
        this.inputStream = inputStream;
        init();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public DownloadState getDownloadState() {
        return downloadState;
    }

    public void setDownloadStateCallback(DownloadStateCallback downloadStateCallback) {
        this.downloadStateCallback = downloadStateCallback;
    }

    private void init() {
        try {
            //解析m3u8
            String m3u8File = cacheDir + name + ".m3u8";
            FileUtil.writeStreamToFile(inputStream,new File(m3u8File));
            Parser.parse(new FileInputStream(m3u8File),this);
            getKey();
        } catch (IOException e) {
            e.printStackTrace();
            Log.log("解析m3u8失败");
        }
    }

    public void addTS(float duration, String url){
        body.add(new TS(duration,url,body.size()));
    }

    public void addSubM3u8(String url,String info,Map<String,String> infoMap){
        subM3u8s.add(new SubInfo(url,info,infoMap));
    }

    public boolean hasSubM3u8(){
        return subM3u8s.size()!=0;
    }

    public void download() throws IOException {
        downloadState=DownloadState.Downloading;
        if (hasSubM3u8()){
            downloadSubM3u8().download();
        }else{
            downloadBodiesWithPool();
        }
    }

    public M3u8 downloadSubM3u8() throws IOException {
        SubInfo subInfo = getMaxM3u8Info(subM3u8s);
        String subPath = subInfo.getUrl();
        String subHost = Parser.assembleHost(host,path,subPath);
        //不创建新的 创建新m3u8会造成一些变量以及回调无法使用
        this.path = subPath;
        this.host = subHost;
        this.inputStream = downloader.getStream(host+path);
        this.subM3u8s = new ArrayList<>();
        init();
        return this;
    }

    private SubInfo getMaxM3u8Info(List<SubInfo> subM3u8s) {
        SubInfo max = subM3u8s.get(0);
        int bandWidth=0;
        for (SubInfo subInfo:subM3u8s){
            int currentWidth = Integer.parseInt(subInfo.getInfoMap().get("BANDWIDTH"));
            if (currentWidth>bandWidth){
                bandWidth = currentWidth;
                max=subInfo;
            }
        }
        return max;
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
            if (aesKey!=null){
                try {
                    bodyBytes = AesUtil.decrypt(bodyBytes,aesKey);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Log.log("aes解密失败");
                }
            }
            File tsFile = new File(tsObj.getCacheFile());
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
        Map<String, String> keys = Parser.parseHeadToMap(headers.get(Parser.HeadMark.EXT_X_KEY.toString()));
        if (keys==null)return null;
        if (keys.get("METHOD").equalsIgnoreCase("AES-128")){
            String keyUrl = keys.get("URI");
            if (keyUrl==null)return null;
            aesKey = downloader.getString(Parser.assembleHost(host,path,keyUrl) + keyUrl);
        }
        return aesKey;
    }

    public class SubInfo{
        private String url;
        private String info;
        private Map<String,String> infoMap;

        public SubInfo(String url, String info, Map<String, String> infoMap) {
            this.url = url;
            this.info = info;
            this.infoMap = infoMap;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public Map<String, String> getInfoMap() {
            return infoMap;
        }

        public void setInfoMap(Map<String, String> infoMap) {
            this.infoMap = infoMap;
        }
    }

    public class TS{
        private int serial;
        private float duration;
        private String url;
        private String tsFile;
        private boolean downloaded;

        public TS(float duration, String url,Integer serial) {
            this.duration = duration;
            this.url = url;
            this.serial = serial;
        }

        public String getHost(){
            return Parser.assembleHost(host,path,url);
        }

        public String getCacheFolder(){
            return cacheDir+name+"/";
        }

        public String getCacheFile(){
            return getCacheFolder()+getSerial()+"-"+url.substring(url.lastIndexOf("/")+1);
        }

        public String getAesKey(){
            return aesKey;
        }

        public int getSerial() {
            return serial;
        }

        public void setSerial(int serial) {
            this.serial = serial;
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
            int current=0;
            for (TS t:body){
                if (!t.isDownloaded()){
                    isAllDownload=false;
                }else {
                    current++;
                }
            }
            if (downloadStateCallback!=null)downloadStateCallback.progress(body.size(),current);
            if (isAllDownload) downloadCallback.onComplete(body);
        }
    };

    DownloadCallback downloadCallback = new DownloadCallback() {
        @Override
        public void onComplete(List<TS> tsList) throws IOException {
            File writeFile = new File(baseDir + name);
            File parent = new File(writeFile.getParent());
            if (!parent.exists())parent.mkdirs();
            FileOutputStream fos = new FileOutputStream(writeFile);
            for (TS ts:tsList){
                byte[] buffer = FileUtil.readBytesFromFile(ts.getTsFile());
                fos.write(buffer);
            }
            fos.flush();
            fos.close();
            Log.log("合并完成");
            downloadState = DownloadState.Complete;
            if (downloadStateCallback!=null) downloadStateCallback.complete(writeFile.getPath());
        }
    };

    public enum DownloadState{
        Init,Downloading,Complete
    }

    interface DownloadCallback {
        void onComplete(List<TS> tsList) throws IOException;
    }

    public interface DownloadStateCallback{
        void progress(int max,int current);
        void complete(String path);
    }

}
