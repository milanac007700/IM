package com.milanac007.demo.im.net;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zqguo on 2016/10/17.
 */
public class CustomFileDownload extends AsyncTask<Void, Integer, String> {
    public  static final String TAG = "CustomFileDownload";

    private final String mUrl;
    private final File mFile;
    private final DownloadListener mListener;

    public interface DownloadListener {
         void onDownloadEnd(JSONObject result);
         void onProgress(int value); //进度
    }

    public CustomFileDownload(String url, File file, DownloadListener listener){
        super();
        mFile = file;
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        OutputStream output = null;
        InputStream input = null;
        StringBuilder reslut = new StringBuilder();

        try {
            // 解析url，并创建一个HttpURLConnection 或 HttpsURLConnection
            HttpURLConnection urlConn = getHttpURLConnection(mUrl);

            urlConn.setRequestMethod("GET");
            /**
             * ConnectTimeout只有在网络正常的情况下才有效，而当网络不正常时，ReadTimeout才真正的起作用，
             * 即IdIOHandlerStack 里的 WaitFor 是受ReadTimeout限制的，因此，这2个属性应该结合实用。
             connect timeout 是建立连接的超时时间；
             read timeout，是传递数据的超时时间。
             */
            urlConn.setReadTimeout(60 * 1000);
            urlConn.setConnectTimeout(60* 1000);
            //TODO
            urlConn.setRequestProperty("clientId", Utils.getDeviceId());
            urlConn.setRequestProperty("refreshToken", Preferences.getRefreshToken());

            //获取内容长度
            int contentLength = urlConn.getContentLength();
            int responseCode = urlConn.getResponseCode();

            input = urlConn.getInputStream();

            if(responseCode == 200){ //正常数据流

                output = new FileOutputStream(mFile);
                byte[] buffer = new byte[1024];
                long totalReaded = 0;
                int inputSize;
                while ((inputSize = input.read(buffer)) != -1){

                    totalReaded += inputSize;
                    int progress = (int)totalReaded * 100 / contentLength;
                    if(progress%10 == 0)
                        publishProgress(progress);

                    output.write(buffer, 0, inputSize);
                }
                output.flush();

            }else { //error
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = bufReader.readLine()) != null)
                    reslut.append(line);
            }

            urlConn.disconnect();

            return reslut.toString();

        }catch (Exception e){
            e.printStackTrace();
            JSONObject resultObject = new JSONObject();
            resultObject.put("error", e.getMessage());
            return resultObject.toJSONString();
        }finally {

            try {
                if(output != null){
                    output.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

            try {
                if (input != null){
                    input.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mListener != null) {
            int progress = values[0];
            if(progress > 0 && progress<=100) {
                mListener.onProgress(progress);
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {

        JSONObject output = new JSONObject();

        if(TextUtils.isEmpty(result)){
            output.put("success", true);
            output.put("downloadUrl", mUrl);
            output.put("localPath", mFile.getPath());
        }else {
            JSONObject input = JSONObject.parseObject(result);
            Logger.getLogger().i(input.toJSONString());

            output.put("success", false);
            if(input.getIntValue("status") == 202){  //need update refreshToken
                String refreshToken = input.getString("refreshToken");
                if(!TextUtils.isEmpty(refreshToken)){
                    Preferences.setRefreshToken(refreshToken);
                }
                output.put("error", "token过期, 请重新登录");
            }else {
                output.put("error", input.getString("message"));
            }
        }

        Logger.getLogger().i(output.toJSONString());

        if (mListener!=null) {
            mListener.onDownloadEnd(output);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Logger.getLogger().i("onCancelled()");
        if (mListener!=null) {
            JSONObject input = new JSONObject();
            input.put("success", false);
            input.put("error", "onCancelled");
            mListener.onDownloadEnd(input);
        }
    }

    private HttpURLConnection getHttpURLConnection(String requestUrl) throws IOException, GeneralSecurityException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(requestUrl);
        if (url.getProtocol().equalsIgnoreCase("HTTPS")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
//            setSSLSocketFactory((HttpsURLConnection)urlConnection);
            setSSLSocketFactory2((HttpsURLConnection)urlConnection);
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        return urlConnection;
    }

    //客户端不校验服务器证书
    private void setSSLSocketFactory2(HttpsURLConnection httpsURLConn) throws IOException, GeneralSecurityException {
        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{tm}, new SecureRandom());
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        httpsURLConn.setSSLSocketFactory(ssf);
    }

    //客户端校验服务器证书
    private void setSSLSocketFactory(HttpsURLConnection httpsURLConn) throws IOException, GeneralSecurityException {
        SSLSocketFactory ssf = null;
        List<byte[]> inBytesList = new ArrayList<>();
//        String[] cerFiles = {"client.pem", "client_sm.pem"};
        String[] cerFiles = {"client.pem"};
//        String[] cerFiles = {"client_sm.pem"};

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
        ssf = getSSLSocketFactory(x509TrustManager);
        httpsURLConn.setSSLSocketFactory(ssf);
    }


    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))){
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    private javax.net.ssl.SSLSocketFactory getSSLSocketFactory(X509TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        return context.getSocketFactory();
    }

    private X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
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

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; //By convention, 'null' creates an empty keystore
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
