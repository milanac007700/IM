package com.milanac007.demo.im.net;

import com.milanac007.demo.im.App;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpTest {
    public final static String urlString = "https://192.168.8.130:8080";

    private X509TrustManager getTrustManager() throws IOException, GeneralSecurityException {
        List<byte[]> inBytesList = new ArrayList<>();
        String[] cerFiles = {"user_chain.crt"};
//        String[] cerFiles = {"ca-chain.crt"};

        int len = 0;
        for(String cerFile : cerFiles) {
            InputStream in = App.getContext().getAssets().open(cerFile);
            byte[] inBytes = toByteArray(in);
            inBytesList.add(inBytes);
            len += inBytes.length;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
        for(byte[] inbytes : inBytesList) {
            bos.write(inbytes);
        }

        ByteArrayInputStream ins = new ByteArrayInputStream(bos.toByteArray());
        return trustManagerForCertificates(ins);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))){
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }


    private static X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        //通过证书工厂得到自签名证书对象组合
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if(certificates.isEmpty()){
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        //为证书设置一个keyStore
        char[] password = "password".toCharArray(); //Any password will work
        KeyStore keyStore = newEmptyKeyStore(password);

        int index = 0;
        //将证书放入keyStore中
        for(Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if(trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" +
                    Arrays.toString(trustManagers));
        }

        return (X509TrustManager)trustManagers[0];

    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; //By convention, 'null' creates an empty keystore
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }


    private static javax.net.ssl.SSLSocketFactory getSSLSocketFactory(X509TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        return context.getSocketFactory();
    }

    private void setSSLSocketFactory(HttpsURLConnection httpsURLConn) throws IOException, GeneralSecurityException {
        X509TrustManager x509TrustManager = getTrustManager();
        SSLSocketFactory ssf = getSSLSocketFactory(x509TrustManager);
        httpsURLConn.setSSLSocketFactory(ssf);
    }

    private HttpURLConnection getHttpURLConnection(String requestUrl) throws IOException, GeneralSecurityException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(requestUrl);
        if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
            setSSLSocketFactory((HttpsURLConnection)urlConnection);
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        return urlConnection;
    }

    public void getByHttpsURLConnection() {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        int IO_BUFFER_SIZE = 8 * 1024;
        try {
            // 解析url，并创建一个HttpURLConnection 或 HttpsURLConnection
            urlConnection = getHttpURLConnection(urlString);
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            System.out.println("connect " + urlString);
            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                System.out.println(new String(buf, 0, len));
            }

            System.out.println("\n\nconnect " + urlString + " success.");
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            try {
                in.close();
            } catch (IOException ignored) { }
        }
    }

    private OkHttpClient mOkHttpClient;
    private OkHttpClient mOkHttpsClient;

    //https时，客户端校验服务器证书
    public OkHttpClient getOkHttpClientInstance(String url){
        try {
            URL requestUrl = new URL(url);
            if(requestUrl.getProtocol().toUpperCase().equals("HTTPS")) {
                return getTrustAllHostsInstance();
            }else {
                if(mOkHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .connectTimeout(5000, TimeUnit.MILLISECONDS)
                            .readTimeout(15000, TimeUnit.MILLISECONDS);
                    mOkHttpClient = builder.build();
                }
                return mOkHttpClient;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private  OkHttpClient getTrustAllHostsInstance(){
        if(mOkHttpsClient == null){
            try {
                X509TrustManager x509TrustManager = getTrustManager();
                SSLSocketFactory ssf = getSSLSocketFactory(x509TrustManager);
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .readTimeout(15000, TimeUnit.MILLISECONDS)
                        .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                        .sslSocketFactory(ssf, x509TrustManager);
                mOkHttpsClient = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mOkHttpsClient;
    }

    public void getByOkHttpClient() {
        Request.Builder requsetBuilder = new Request.Builder();
        Request request = requsetBuilder.url(urlString).get().build();
        ResponseBody responseBody = null;
        String rsp = null;
        try {
            System.out.println("connect " + urlString);
            Call call = getOkHttpClientInstance(urlString).newCall(request);
            Response response = call.execute();
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            responseBody = response.body();
            rsp = responseBody.string();
            System.out.println(rsp);

            System.out.println("\n\nconnect " + urlString + " success.");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
