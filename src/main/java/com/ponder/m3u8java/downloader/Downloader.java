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

        public TSRunnable(M3u8.TS ts, TsDownloadCallback callback) {
            this.ts = ts;
            this.callback = callback;
        }

        @Override
        public void run() {
            String tsUrl = ts.getUrl();
            byte[] bodyBytes = new byte[0];
            boolean isGet = false;
            while (!isGet){
                isGet = getBytesForRetry(tsUrl,bodyBytes);
            }
            try {
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

        private boolean getBytesForRetry(String tsUrl, byte[] bodyBytes) {
            try {
                bodyBytes = getBytes(ts.getHost() + tsUrl);
                if (ts.getAesKey()!=null){
                    bodyBytes = AesUtil.decrypt(bodyBytes,ts.getAesKey());
                }
            }catch (SocketTimeoutException timeoutException){
                Log.log("java.net.SocketTimeoutException: Read timed out; I will retry for it");
                return false;
            }catch (Exception e){
                e.printStackTrace();
                Log.log("aes解密失败");
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
