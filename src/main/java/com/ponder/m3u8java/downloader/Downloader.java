package com.ponder.m3u8java.downloader;

import com.ponder.m3u8java.aes.AesUtil;
import com.ponder.m3u8java.util.FileUtil;
import com.ponder.m3u8java.base.M3u8;
import com.ponder.m3u8java.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
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

        private byte[] bodyBytes;

        public TSRunnable(M3u8.TS ts, TsDownloadCallback callback) {
            this.ts = ts;
            this.callback = callback;
        }

        @Override
        public void run() {
            boolean isGet = false;
            while (!isGet){
                isGet = getBytesForRetry();
            }
            try {
                if (ts.getAesKey()!=null) bodyBytes = AesUtil.decrypt(bodyBytes,ts.getAesKey());
                File tsFile = new File(ts.getCacheFile());
                FileUtil.writeBodyBytesToFile(bodyBytes,tsFile);
                ts.setTsFile(tsFile.toString());
                ts.setDownloaded(true);
                Log.log(Thread.currentThread().getId() + "下载完成" + ts.getTsFile());
                callback.onTsDownloaded(ts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean getBytesForRetry() {
            try {
                String url;
                if (ts.getUrl().startsWith("http")){
                    url = ts.getUrl();
                }else{
                    url = ts.getHost()+ts.getUrl();
                }
                bodyBytes = getBytes(url);
            } catch (IOException e) {
                Log.log(ts.getSerial() + "SocketException; I will retry for it" + e.getCause());
                return false;
            }
            return true;
        }
    }

    public interface TsDownloadCallback{
        void onTsDownloaded(M3u8.TS ts) throws IOException;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
