package com.ponder.m3u8java;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownLoadRunnable implements Runnable {

    public static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static OkHttpClient client = new OkHttpClient();

    private M3u8.TS ts;
    private M3u8 m3u8;
    private TsDownloadComplete tsDownloadComplete;

    public DownLoadRunnable(M3u8.TS ts,M3u8 m3u8,TsDownloadComplete tsDownloadComplete) {
        this.ts = ts;
        this.m3u8 = m3u8;
        this.tsDownloadComplete = tsDownloadComplete;
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
            System.out.println("下载完成"+ts.getUrl());
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

    interface TsDownloadComplete{
        void onTsDownloaded(M3u8.TS ts) throws IOException;
    }

}
