package com.ponder.m3u8java;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 18:25
 */
public class Downloader {

    private String url;
    private OkHttpClient client = new OkHttpClient();
    private Request request;

    public Downloader(String url) {
        this.url = url;
        request = new Request.Builder()
                .url(url)
                .build();
    }

    public InputStream download() throws IOException {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful() || response.body()==null){
            throw new RuntimeException("m3u8 list下载失败");
        }
        return response.body().byteStream();
    }


}
