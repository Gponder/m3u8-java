package com.ponder.m3u8java.downloader;

import com.ponder.m3u8java.aes.AesUtil;
import com.ponder.m3u8java.util.FileUtil;
import com.ponder.m3u8java.base.M3u8;
import com.ponder.m3u8java.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Downloader{

    private ExecutorService executorService;

    public Downloader(int thread) {
        this.executorService = Executors.newFixedThreadPool(thread);
    }

    public abstract InputStream getStream(String url) throws IOException;

    public abstract byte[] getBytes(String url) throws IOException;

    public abstract String getString(String url) throws IOException;

    public void addTSDownloadTask(M3u8.TS ts,TsDownloadCallback callback){
        executorService.execute(new TSRunnable(ts,callback));
    }

    class TSRunnable implements Runnable{
        private M3u8.TS ts;
        private TsDownloadCallback callback;

        public TSRunnable(M3u8.TS ts, TsDownloadCallback callback) {
            this.ts = ts;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                String tsUrl = ts.getUrl();
                byte[] bodyBytes = getBytes(ts.getHost() + tsUrl);
                if (ts.getAesKey()!=null){
                    try {
                        bodyBytes = AesUtil.decrypt(bodyBytes,ts.getAesKey());
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.log("aes解密失败");
                    }
                }
                File tsFile = new File(ts.getCacheFile());
                FileUtil.writeBodyBytesToFile(bodyBytes,tsFile);
                ts.setTsFile(tsFile.toString());
                ts.setDownloaded(true);
                Log.log(Thread.currentThread().getId() + "下载完成" + ts.getTsFile());
                callback.onTsDownloaded(ts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface TsDownloadCallback{
        void onTsDownloaded(M3u8.TS ts) throws IOException;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
