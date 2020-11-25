package com.ponder.m3u8java.util;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/11/25 9:31
 */
public class AddCert {

    public static void addCertNoException(URLConnection connection){
        if (connection instanceof HttpsURLConnection){
            try {
                addCert((HttpsURLConnection) connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addCert(HttpsURLConnection connection) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, ProtocolException {
        //KeyStore trustStore = getKeyStore("jks", "E:\\ts\\cacerts", "");
        //不能放在resources根目录下
        KeyStore trustStore = getKeyStore("jks", ClassLoader.getSystemResourceAsStream("ssl/cacerts"), "");
        KeyManager[] keyManagers = null;
        TrustManager[] trustManagers = null;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        trustManagers = trustManagerFactory.getTrustManagers();

        //设置服务端支持的协议
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(keyManagers, trustManagers, null);
        SSLSocketFactory sslFactory = context.getSocketFactory();

        connection.setSSLSocketFactory(sslFactory);
        //验证URL的主机名和服务器的标识主机名是否匹配
        connection.setHostnameVerifier((hostname,session)-> true);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    /**
     * 获取证书
     * @return
     */
    private static KeyStore getKeyStore(String type, String filePath, String password) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return getKeyStore(type, in, password);
    }

    public static KeyStore getKeyStore(String type, InputStream in, String password){
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(type);
            keyStore.load(in, password.toCharArray());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyStore;
    }
}
