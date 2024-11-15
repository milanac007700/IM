package com.milanac007.demo.im.net;

/**
 * Created by zqguo on 2016/10/14.
 */

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.logger.Logger;
import com.milanac007.demo.im.utils.Preferences;
import com.milanac007.demo.im.utils.Utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * 已登录 ，有相关token
 * 文件上传
 */

public class CustomFileUpload extends AsyncTask<Void, Integer, String> {
    public  static final String TAG = "CustomFileUpload";
    private String actionUrl;
    private List<File> files;
    private UploadListener listener;
    private int fileType;
    private int useType;
    private DataOutputStream outStream;
    private HttpURLConnection conn;


    public interface UploadListener {
        public void onUploadEnd(JSONObject result);
        public void onProgress(int value); //进度
    }

    public CustomFileUpload(String actionUrl, List<File> files, final int fileType, int useType, UploadListener listener) {

        this.actionUrl = actionUrl;
        this.files = files;
        this.fileType = fileType;
        this.useType = useType;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        startTimeout(60); //60秒强制设为超时消息
    }

//    @Override
//    protected String doInBackground(Void... arg0) {
//        String result = new NetRequestByOkHttpClient().uploadFiles(actionUrl, fileType, files);
//        cancelTimeout();
//        return result;
//    }

    @Override
    protected String doInBackground(Void... arg0) {
        String result = null;
        try {
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String PREFIX = "--", LINEND = "\r\n";
            String MULTIPART_FROM_DATA = "multipart/form-data";
            String CHARSET = "UTF-8";

            // 解析url，并创建一个HttpURLConnection 或 HttpsURLConnection
            conn = getHttpURLConnection(actionUrl);

            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60* 1000);

            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA  + ";boundary=" + BOUNDARY);
            //TODO
            conn.setRequestProperty("clientId", Utils.getDeviceId());
            conn.setRequestProperty("refreshTokenGrantType", "refresh_token");
            conn.setRequestProperty("refreshToken", Preferences.getRefreshToken());
//            conn.setRequestProperty("tokenValue", Preferences.getAccessToken());
            conn.setRequestProperty("fileType", String.valueOf(fileType)); //件类型fileType（音频1，视频2，图片3，可执行文件等其他文件类型为4）
            conn.setRequestProperty("useType", String.valueOf(useType)); //文件用途useType（私聊文件1、群文件2、公告文件3、朋友圈文件4，私有文件5等）

            outStream = new DataOutputStream(conn.getOutputStream());

            // 发送文件数据
            long contentLength = 0;
            if (files != null)
                for(File file : files){
                    //获取内容长度
                     contentLength += file.length();
                }

                for (File file : files) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""   + file.getName() + "\"" + LINEND);
                    sb1.append("Content-Type: multipart/form-data; charset="  + CHARSET + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());
                    File valuefile = file;
                    InputStream is = new FileInputStream(valuefile);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    int totalReaded = 0;
                    while ((len = is.read(buffer)) != -1) {
                        totalReaded += len;
                        int progress = (int)(totalReaded*100/contentLength);
                        if(progress%10 == 0)
                            publishProgress(progress);

                        outStream.write(buffer, 0, len);

                    }
                    is.close();
                    outStream.write(LINEND.getBytes());
                }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();

            // 得到响应码
            InputStream in = conn.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String line = null;
            result = "";
            while ((line = bufReader.readLine()) != null)
                result += line;

            outStream.close();
            conn.disconnect();

        }catch (SocketTimeoutException e) {
            JSONObject input = new JSONObject();
            input.put("status", -1);
            input.put("message", "发送超时，请稍候重试");
            result = input.toJSONString();
        }
        catch (SSLException e) {
            e.printStackTrace();
            JSONObject input = new JSONObject();
            input.put("status", -1);
            input.put("message", e.getMessage());
            result = input.toJSONString();
        }
        catch (ConnectException e){
            JSONObject input = new JSONObject();
            input.put("status", -1);
            input.put("message", "连接超时，请检查网络");
            result = input.toJSONString();
        }
        catch (FileNotFoundException e){
            JSONObject input = new JSONObject();
            input.put("status", -1);
            input.put("message", "服务器异常");
            result = input.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger().e("%s", e.getMessage());
            JSONObject input = new JSONObject();
            input.put("status", -1);
            input.put("message", e.getMessage());
            result = input.toJSONString();
        }

        cancelTimeout();
        return result;
    }


    @Override
    protected void onCancelled() {
        //取消操作
        Logger.getLogger().i("%s", "onCancelled() called");
        cancelTimeout();
        if (listener!=null) {
            JSONObject input = new JSONObject();
            input.put("success", false);
            input.put("status", -1);
            input.put("message", "取消操作");
            Logger.getLogger().e("%s", input.toJSONString());
            listener.onUploadEnd(input);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果

        if(TextUtils.isEmpty(result))
            return;

        JSONObject input = JSONObject.parseObject(result);
        JSONObject output = new JSONObject();

        if(input != null){
            int status = input.getIntValue("status");
            if(status == 200){
                JSONArray pathList = input.getJSONArray("pathList");
                output.put("uploadUrl", pathList);

//                String accessToken = input.getString("accessToken");
//                if(!TextUtils.isEmpty(accessToken)){
//                    Preferences.setAccessToken(accessToken);
//                }

                output.put("success", true);

            }else {
                output.put("success", false);
                output.put("status", status);
                output.put("error", input.getString("message"));
            }
        }else {
            output.put("success", false);
            output.put("error", result);
        }
        Logger.getLogger().e("%s", output.toJSONString());

        if (listener!=null) {
            listener.onUploadEnd(output);
        }

    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        //onProgressUpdate方法用于更新进度信息
        if (listener!=null) {
            int progress = values[0];
            if(progress > 0 && progress<=100) {
                listener.onProgress(progress);
            }
        }
    }

    private void cancelTimeout(){
        if(caculateTimer != null) {
            caculateTimer.cancel();
            caculateTimer = null;
        }

        if(mTimerHander != null)
            mTimerHander = null;
    }

    private Timer caculateTimer;
    private void startTimeout(long delaySecs) {
        if(caculateTimer == null) {
            caculateTimer = new Timer();
        }else {
            caculateTimer.cancel();
        }

        caculateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mTimerHander.sendEmptyMessage(0);
            }
        },  delaySecs*1000);
    }

    private Handler mTimerHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message.what == 0){
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    if(mTimerHander!= null){
//                        JSONObject input = new JSONObject();
//                        input.put("status", -1);
//                        input.put("message", "发送超时，请稍候重试");
//                        onPostExecute(input.toJSONString());

                        CustomFileUpload.this.cancel(true);
                    }

                }

            }
            return true;
        }
    });


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
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
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