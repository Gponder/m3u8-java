package com.ponder.m3u8java.downloader.urlconnection;

import com.ponder.m3u8java.downloader.Downloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class UrlDownloader extends Downloader {
    public UrlDownloader(int thread) {
        super(thread);
    }

    @Override
    public InputStream getStream(String url) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        setTimeOut(urlConnection);
        urlConnection.connect();
        return urlConnection.getInputStream();
    }

    private void setTimeOut(URLConnection urlConnection) {
        urlConnection.setConnectTimeout(15*1000);
        urlConnection.setReadTimeout(15*1000);
    }

    @Override
    public byte[] getBytes(String url) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        setTimeOut(urlConnection);
        urlConnection.connect();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = urlConnection.getInputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1){
            bos.write(buffer,0,len);
        }
        bos.flush();;
        bos.close();
        is.close();
        return bos.toByteArray();
    }

    @Override
    public String getString(String url) throws IOException {
        return new String(getBytes(url), StandardCharsets.UTF_8);
    }
}
