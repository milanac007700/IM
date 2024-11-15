package com.milanac007.demo.im.net;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

public class NetRequestByOkHttpClient {
    private static final String TAG  = "OkHttpClient";

    private static String[] headers;
    private static String[] getHeaders;
    private static OkHttpClient mOkHttpsClient;
    private static OkHttpClient mOkHttpClient;
    private static OkHttpClient mOkHttpsClient2;

    public static MediaType JSON = MediaType.parse("application/json");//application/json;charset=utf-8

    static{
        if(headers == null){
            headers = new String[4];
            headers[0] = "Charset:UTF-8";// 设置编码格式
            headers[1] = "User-Agent:Android";// 传递自定义参数 header
            headers[2] = "Accept:*/*";
            headers[3] = "Content-Type:" + "application/json;";
        }

        if(getHeaders == null){
            getHeaders = new String[4];
            getHeaders[0] = "Charset:UTF-8";// 设置编码格式
            getHeaders[1] = "User-Agent:Android";// 传递自定义参数 header
            getHeaders[2] = "Accept:*/*";
            getHeaders[3] = "Content-Type:" + "application/json;";
        }
    }


    private static OkHttpClient getTrustAllHostsInstance(){
        //方式二：客户端单向校验服务器证书
        if(mOkHttpsClient == null){
            try {
                List<byte[]> inBytesList = new ArrayList<>();
                //        String[] cerFiles = {"client.pem", "client_sm.pem"};
                        String[] cerFiles = {"client.pem"};
//                String[] cerFiles = {"client_sm.pem"};

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

                X509TrustManager x509TrustManager = trustManagerForCertificates(ins);
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .readTimeout(15000, TimeUnit.MILLISECONDS)
                        .hostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                        .sslSocketFactory(getSSLSocketFactory(x509TrustManager), x509TrustManager);
                mOkHttpsClient = builder.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mOkHttpsClient;
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

    private static javax.net.ssl.SSLSocketFactory getSSLSocketFactory(X509TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        return context.getSocketFactory();
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

        //use it to build an X509 trust manager
        //使用包含自签名证书信息的Keystore构建一个X509TrustManager
//        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        keyManagerFactory.init(keyStore, password);

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

    //https时，客户端校验服务器证书
    public static OkHttpClient getInstance(String url){
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

    private OkHttpClient getOkHttpsClient2Instance(String url) {
        try {
            URL requestUrl = new URL(url);
            if(requestUrl.getProtocol().toUpperCase().equals("HTTPS")) {
                //方式一：客户端不校验服务器证书
                if(mOkHttpsClient2 == null){
                    try {
                        TrustManager tm = new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        };
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{tm}, new SecureRandom());
                        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                                .readTimeout(15000, TimeUnit.MILLISECONDS)
                                .sslSocketFactory(sslContext.getSocketFactory());
                        builder.hostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        mOkHttpsClient2 = builder.build();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return mOkHttpsClient2;

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

    public String postRequest(final String url, final String params) {
        if(!Utils.isNetworkAvailable()){
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            return exception.toJSONString();
        }

        Request.Builder requsetBuilder = new Request.Builder();
        for (String h : headers){
            String[] split = h.split(":");
            requsetBuilder.addHeader(split[0], split[1]);
        }

        if(!TextUtils.isEmpty(Preferences.getRefreshToken())) {
            requsetBuilder.addHeader("Authorization", Preferences.getRefreshToken());
        }

        String deviceId = Utils.getDeviceId();
        if(!TextUtils.isEmpty(deviceId)) {
            requsetBuilder.addHeader("deviceId", deviceId);
        }

        RequestBody requestBody = RequestBody.create(JSON, params.getBytes(Util.UTF_8));
        Request request = requsetBuilder.url(url).post(requestBody).build();
        ResponseBody responseBody = null;
        String rsp = null;
        try {
//            Call call = getInstance(url).newCall(request);
            Call call = getOkHttpsClient2Instance(url).newCall(request);
            Response response = call.execute();
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            String refreshToken = response.header("Authorization");
            if(!TextUtils.isEmpty(refreshToken)){
                Preferences.setRefreshToken(refreshToken);
            }

            responseBody = response.body();
            rsp = responseBody.string();

        }catch (SocketException | SSLException | SocketTimeoutException e){
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            rsp = exception.toJSONString();
        }catch (IOException e) {
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", e.getMessage());
            rsp = exception.toJSONString();
        } finally {
            if(responseBody != null) {
                responseBody.close();
            }
        }
        Log.i(TAG, "url： "+ url + " |rsp: " + rsp);
        return rsp;
    }

    public String get(final String url)  {
        if(!Utils.isNetworkAvailable()){
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            return exception.toJSONString();
        }

        Request.Builder requsetBuilder = new Request.Builder();
        for (String h : headers){
            String[] split = h.split(":");
            requsetBuilder.addHeader(split[0], split[1]);
        }

        String deviceId = Utils.getDeviceId();
        if(!TextUtils.isEmpty(deviceId)) {
            requsetBuilder.addHeader("deviceId", deviceId);
        }

        Request request = requsetBuilder.url(url).get().build();
        ResponseBody responseBody = null;
        String rsp = null;
        try {
            Call call = getInstance(url).newCall(request);
            Response response = call.execute();
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            responseBody = response.body();
            rsp = responseBody.string();

            JSONObject rspObject =  JSONObject.parseObject(rsp);
            if(rspObject != null && rspObject.containsKey("resultCode") && !rspObject.getString("resultCode").equals("0")) {
                JSONObject exception = new JSONObject();
                String errorMsg = rspObject.getString("errorMsg");
                exception.put("resultCode", -1);
                exception.put("errorMsg", errorMsg);
                rsp = exception.toJSONString();
            }

        }catch (SocketException | SocketTimeoutException e){
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            rsp = exception.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", e.getMessage());
            rsp = exception.toJSONString();
        }finally {
            Log.i(TAG, "url： "+ url + " |rsp: " + rsp);
            return rsp;
        }
    }



    /**
     * 用于访问不需要校验服务器证书的接口
     */
    public String get2(final String url)  {
        if(!Utils.isNetworkAvailable()){
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            return exception.toJSONString();
        }

        Request.Builder requsetBuilder = new Request.Builder();
        for (String h : headers){
            String[] split = h.split(":");
            requsetBuilder.addHeader(split[0], split[1]);
        }

        Request request = requsetBuilder.url(url).get().build();
        ResponseBody responseBody = null;
        String rsp = null;
        try {
            Call call = getOkHttpsClient2Instance(url).newCall(request);
            Response response = call.execute();
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
            responseBody = response.body();
            rsp = responseBody.string();
        }catch (SocketException | SocketTimeoutException e){
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            rsp = exception.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", e.getMessage());
            rsp = exception.toJSONString();
        }finally {
            Log.i(TAG, "url： "+ url + " |rsp: " + rsp);
            return rsp;
        }
    }

    public String getFile(String url, File saveFile, long downloadSize, long fileSize) {
        if(!Utils.isNetworkAvailable()){
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            return exception.toJSONString();
        }

        if(saveFile.exists()) {
            String savePath = saveFile.getAbsolutePath();
            saveFile.delete();
            saveFile = new File(savePath);
        }

        Request.Builder requsetBuilder = new Request.Builder();
        for (String h : headers){
            String[] split = h.split(":");
            requsetBuilder.addHeader(split[0], split[1]);
        }

        if(downloadSize > 0 && downloadSize < fileSize) {
            requsetBuilder.addHeader("Range", "bytes=" + downloadSize + "-" + fileSize);
        }

        Request request = requsetBuilder.url(url).get().build();
        String rsp;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
//            Call call = getInstance(url).newCall(request);
            Call call = getOkHttpsClient2Instance(url).newCall(request);
            Response response = call.execute();
            Log.i(TAG, "下载文件 rsp: responseCode: " + response.code());
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody responseBody = response.body();
            in = new BufferedInputStream(responseBody.byteStream());
            out = new BufferedOutputStream(new FileOutputStream(saveFile));
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            JSONObject result = new JSONObject();
            result.put("resultCode", 0);
            result.put("filePath", saveFile.getPath());
            rsp = result.toJSONString();
        }catch (SocketException | SocketTimeoutException e){
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            rsp = exception.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", e.getMessage());
            rsp = exception.toJSONString();
        }finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }

            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
        return rsp;
    }

    public String uploadFiles(String url, int fileType, List<File> files) {
        if(!Utils.isNetworkAvailable()){
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            return exception.toJSONString();
        }

        String BOUNDARY = java.util.UUID.randomUUID().toString();

        Request.Builder requsetBuilder = new Request.Builder();

        requsetBuilder.addHeader("connection", "keep-alive");
        requsetBuilder.addHeader("fileType", String.valueOf(fileType)); //件类型fileType（音频1，视频2，图片3，可执行文件等其他文件类型为4）
        requsetBuilder.addHeader("Content-Type:", "multipart/form-data;boundary=\"" + BOUNDARY + "\"");

        if(!TextUtils.isEmpty(Preferences.getRefreshToken())) {
            requsetBuilder.addHeader("Authorization", Preferences.getRefreshToken());
        }

        String deviceId = Utils.getDeviceId();
        if(!TextUtils.isEmpty(deviceId)) {
            requsetBuilder.addHeader("deviceId", deviceId);
        }

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        String mediaType = "image/jpg";
        switch (fileType) {
            case 1: {
                mediaType = "audio/wma";
            }break;
            case 2: {
                mediaType = "video/mp4";
            }break;
            case 3:{
                mediaType = "image/jpg";
            }break;
        }

        for(int j = 0;j < files.size();j++){
            File file = files.get(j);
            long fileSize = file.length();
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"file\";filename=\"" + file.getName() +  "\";filesize="+fileSize),
                    RequestBody.create(MediaType.parse(mediaType),files.get(j))
            );
        }

        RequestBody body = builder.build();
        Request request = requsetBuilder.url(url).post(body).build();

        ResponseBody responseBody = null;
        String rsp = null;
        try {
            Call call = getInstance(url).newCall(request);
//            Call call = getOkHttpsClient2Instance(url).newCall(request);
            Response response = call.execute();
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            String refreshToken = response.header("Authorization");
            if(!TextUtils.isEmpty(refreshToken)){
                Preferences.setRefreshToken(refreshToken);
            }

            responseBody = response.body();
            rsp = responseBody.string();

        }catch (SocketException | SSLException | SocketTimeoutException e){
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", "网络异常，请检查网络连接");
            rsp = exception.toJSONString();
        }catch (IOException e) {
            e.printStackTrace();
            JSONObject exception = new JSONObject();
            exception.put("resultCode", -1);
            exception.put("errorMsg", e.getMessage());
            rsp = exception.toJSONString();
        } finally {
            if(responseBody != null) {
                responseBody.close();
            }
        }
        Log.i(TAG, "url： "+ url + " |rsp: " + rsp);
        return rsp;

    }
}
