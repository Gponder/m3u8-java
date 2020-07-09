package com.ponder.m3u8java.downloader.okhttp;

import com.ponder.m3u8java.downloader.Downloader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;

public class OkHttpDownloader extends Downloader{

    private OkHttpClient client = new OkHttpClient();

    public OkHttpDownloader(int thread) {
        super(thread);
    }

    @Override
    public InputStream getStream(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful() || response.body()==null){
            throw new RuntimeException("m3u8 list下载失败");
        }
        return response.body().byteStream();
    }

    @Override
    public byte[] getBytes(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful() || response.body()==null){
            throw new RuntimeException("m3u8 list下载失败");
        }
        return response.body().bytes();
    }

    @Override
    public String getString(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful() || response.body()==null){
            throw new RuntimeException("m3u8 list下载失败");
        }
        return response.body().string();
    }
}
