package com.ponder.m3u8java.downloader.okhttp;

import com.ponder.m3u8java.FileUtil;
import com.ponder.m3u8java.M3u8;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownLoadRunnable implements Runnable {

    public static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static OkHttpClient client;

    private M3u8.TS ts;
    private M3u8 m3u8;
    private TsDownloadComplete tsDownloadComplete;

    public DownLoadRunnable(M3u8.TS ts,M3u8 m3u8,TsDownloadComplete tsDownloadComplete) {
        this.ts = ts;
        this.m3u8 = m3u8;
        this.tsDownloadComplete = tsDownloadComplete;
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void download(){
        executorService.execute(this);
    }

    @Override
    public void run() {
        try {
            String tsUrl = ts.getUrl();
            Request request = new Request.Builder().url(m3u8.getHost() + tsUrl).build();
            byte[] bodyBytes = getBodyBytes(request);
            File tsFile = new File(m3u8.getTsCacheFolder()+tsUrl.substring(tsUrl.lastIndexOf("/")+1));
            FileUtil.writeBodyBytesToFile(bodyBytes,tsFile);
            ts.setTsFile(tsFile.toString());
            ts.setDownloaded(true);
            System.out.println(Thread.currentThread().getId()+"下载完成"+ts.getUrl());
            tsDownloadComplete.onTsDownloaded(ts);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public byte[] getBodyBytes(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful() || response.body()==null){
            throw new RuntimeException("m3u8 list下载失败");
        }
        return response.body().bytes();
    }

    public interface TsDownloadComplete{
        void onTsDownloaded(M3u8.TS ts) throws IOException;
    }

}
