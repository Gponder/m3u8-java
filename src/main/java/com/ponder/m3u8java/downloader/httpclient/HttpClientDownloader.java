package com.ponder.m3u8java.downloader.httpclient;

import com.ponder.m3u8java.downloader.Downloader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpClientDownloader extends Downloader {

    private HttpClient httpClient = HttpClientBuilder.create().build();

    public HttpClientDownloader(int thread) {
        super(thread);
    }

    @Override
    public InputStream getStream(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        return response.getEntity().getContent();
    }

    @Override
    public byte[] getBytes(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        return bos.toByteArray();
    }

    @Override
    public String getString(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        byte[] buffer = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

}
