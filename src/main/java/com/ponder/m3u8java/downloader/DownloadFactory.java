package com.ponder.m3u8java.downloader;

import com.ponder.m3u8java.downloader.httpclient.HttpClientDownloader;
import com.ponder.m3u8java.downloader.okhttp.OkHttpDownloader;
import com.ponder.m3u8java.downloader.urlconnection.UrlDownloader;

public class DownloadFactory {

    private static int poolSize = 8;
    private static Downloader downloader;

    public static synchronized Downloader getDownloader(Type type){
        if (downloader==null){
            if (type == Type.OK_HTTP)downloader = new OkHttpDownloader(poolSize);
            if (type == Type.HTTP_CLIENT)downloader = new HttpClientDownloader(poolSize);
            if (type == Type.URL_CONNECTION)downloader = new UrlDownloader(poolSize);
        }
        return downloader;
    }

    public static void closeThreadPool(){
        if (downloader!=null) downloader.getExecutorService().shutdownNow();
    }

    public static void setPoolSize(int poolSize) {
        DownloadFactory.poolSize = poolSize;
    }

    public enum Type{
        OK_HTTP(1),HTTP_CLIENT(2),URL_CONNECTION(3);
        private final int type;

        Type(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return String.valueOf(type);
        }
    }

}
