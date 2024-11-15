package com.milanac007.demo.im.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.logger.Logger;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;

/**
 * Created by milanac007 on 2018/2/1.
 */

public class Utils {
    public static final String TAG = "Utils";
    static private Logger logger = Logger.getLogger();


    private static int widthPixels = 0;
    private static int heightPixels = 0;


    public static int dp2dx(float v) {
        DisplayMetrics dm = App.getContext().getResources().getDisplayMetrics();
        float scale = dm.density;
        widthPixels = dm.widthPixels;
        return (int) (scale * v + 0.5f);
    }

    public static int getScreenWidth() {
        if (widthPixels == 0) {
            DisplayMetrics dm = App.getContext().getResources().getDisplayMetrics();
            widthPixels = dm.widthPixels;
        }

        return widthPixels;
    }

    public static int getScreenHeight() {
        if (heightPixels == 0) {
            DisplayMetrics dm = App.getContext().getResources().getDisplayMetrics();
            heightPixels = dm.heightPixels;
        }

        return widthPixels;
    }

    /**
     * 是否支持蓝牙模块
     */
    @TargetApi(18)
    public static boolean isSupportBle() {
        Context context = App.getContext();
        if (context != null && context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return manager.getAdapter() != null;
        } else {
            return false;
        }
    }

    /**
     * 手机是否支持OTG
     *
     * @param
     */
    public static boolean isSupportOTG(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.usb.host")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设备厂商
     *
     * @return
     */
    static public String getDeviceManufacturer() {
        return Build.MANUFACTURER.toLowerCase();
    }

    /**
     * 设备名称 如me860
     *
     * @return
     */
    static public String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 系统版本号 1.0
     *
     * @return
     */
    static public String getDeviceSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 当前程序版本号
     *
     * @return
     */
    static public String getAppVersion() {
        String versionName = "";
        try {
            PackageInfo packageInfo = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            return versionName;
        }
    }


    /**
     * 当前程序版本号
     *
     * @return
     */
    static public int getAppVersionCode() {

        try {
            PackageInfo packageInfo = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }


    private static Toast mToast = null;

    public static Toast getToast(String str){
        //暂时解决快速调用该方法，有些版本的手机(华为 Android 10)会不显示toast的bug
        if(mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        //解决小米手机 toast带应用名称的问题
//        mToast = Toast.makeText(App.getContext(), str, Toast.LENGTH_SHORT);
        mToast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
        mToast.setText(str);
        return mToast;
    }

    public static final void showToast(int msgId) {
        CharSequence msg = App.getContext().getResources().getText(msgId);
        showToast(msgId);
    }
    public static final void showToast(final String msg) {
        Log.i(TAG, String.format("showToast(final String msg): Looper.getMainLooper().getThread().getId() = %d, Thread.currentThread().getId() = %d",
                Looper.getMainLooper().getThread().getId(), Thread.currentThread().getId()));

        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            new HandlerPost(0, true){
                @Override
                public void doAction() {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){//Android8.0以前的版本有bug：没有catch BadTokenException
                        ToastCompat.getToast(msg).show();
                        return;
                    }
                    getToast(msg).show();
                }
            };
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                ToastCompat.getToast(msg).show();
                return;
            }
            getToast(msg).show();
        }
    }

    public static void hideToast(){
        if(mToast != null){
            mToast.cancel();
            mToast = null;
        }
    }

    /**
     * 判断当前设备是否为平板
     *
     * @return 平板true, 手机false
     */
    static public boolean isPad() {
        return (App.getContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;

    }

     public static String getBuildInfo() {
         //这里选用了几个不会随系统更新而改变的值
         StringBuffer buildSB = new StringBuffer();
         buildSB.append(Build.BRAND).append("/");
         buildSB.append(Build.PRODUCT).append("/");
         buildSB.append(Build.DEVICE).append("/");
         buildSB.append(Build.ID).append("/");
         buildSB.append(Build.VERSION.INCREMENTAL);
         return buildSB.toString();
    }

    public static String getDeviceUUID(Context context) {
        String uuid = loadDeviceUUID(context);
        if (TextUtils.isEmpty(uuid)) {
            uuid = buildDeviceUUID(context);
            saveDeviceUUID(context, uuid);
        }
        return uuid;
    }

    private static String buildDeviceUUID(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if ("9774d56d682e549c".equals(androidId)) {
            Random random = new Random();
            androidId = Integer.toHexString(random.nextInt())
                    + Integer.toHexString(random.nextInt())
                    + Integer.toHexString(random.nextInt());
        }
        Log.i("Utils", "androidId: " + androidId + ",getBuildInfo(): " + getBuildInfo());
        //使用指定的数据构造一个新的{@code UUID}。
        //mostSigBits: 最高有效64位; minimumSigBits:最低有效64位
        return new UUID(androidId.hashCode(), getBuildInfo().hashCode()).toString();
    }

    private static void saveDeviceUUID(Context context, String uuid) {
        context.getSharedPreferences("device_uuid", Context.MODE_PRIVATE)
                .edit()
                .putString("uuid", uuid)
                .apply();
    }

    @Nullable
    private static String loadDeviceUUID(Context context) {
        return context.getSharedPreferences("device_uuid", Context.MODE_PRIVATE)
                .getString("uuid", null);
    }

    /**
     * 获取设备号
     *
     * Device ID：
     * 开发者可以使用系统提供的TelephonyManager服务来获取Device ID，GSM设备返回的是IMEI码，CDMA设备返回的是MEID码或者ESN码。
     * 使用Device ID的优点在于重复率低，如果返回了Device ID，可以保证每个ID都是唯一的，但缺点也很明显，首先平板电脑等非手机设备
     * 无法提供Device ID，其次6.0以后需要用户动态授予READ_PHONE_STATE权限，如果用户拒绝就无法获得Device ID了。
     *
     * ANDROID_ID:
     * 在设备首次启动时，系统会随机生成一个64位的数字，并把这个数字以16进制字符串的形式保存下来，这个16进制的字符串就是ANDROID_ID，当设备被wipe后该值会被重置。
     * Android ID是一个不错的选择，64位的随机数重复率不高，而且不需要申请权限，但也有些小问题，比如有个很常见的Bug会导致设备产生相同的Android ID: 9774d56d682e549c，
     * 另外Android ID的生成不依赖硬件，刷机或者升级系统（这个没验证过）可能都会改变Android ID。
     *
     * Mac地址：
     * WLAN Mac地址和Bluetooth Mac地址都是与硬件相关的唯一号码，分别需要ACCESS_WIFI_STATE和BLUETOOTH权限，其中WLAN Mac地址经常被作为参数
     * 来生成设备标识，但是在Android 6.0（iOS 7）以后，官方以保护用户隐私为由关闭了获取Mac地址的接口，
     * 调用获取的方法会统一返回 02:00:00:00:00:00
     *
     * Build信息：
     * android.os.Build类包含了很多设备信息，包括系统版本号、手机型号等硬件软件信息,Build信息无需任何权限可以直接调用获取，
     * 同时使用多个信息的话一般不会都为空，缺点是重复率很高，同型号手机的Build信息很可能完全相同，而且系统升级等不属于更换设备的操作可能会修改到其中的内容，
     * 所以只考虑作为生成设备ID 的辅助参数。
     * public static String getBuildInfo() {
     * //这里选用了几个不会随系统更新而改变的值
     * StringBuffer buildSB = new StringBuffer();
     * buildSB.append(Build.BRAND).append("/");
     * buildSB.append(Build.PRODUCT).append("/");
     * buildSB.append(Build.DEVICE).append("/");
     * buildSB.append(Build.ID).append("/");
     * buildSB.append(Build.VERSION.INCREMENTAL);
     * return buildSB.toString();
     * // return Build.FINGERPRINT;
     * }
     *
     * 综上所述，Device ID和Mac地址都需要额外的权限，而且并非适用于所有Android设备，所以不考虑使用。最
     * 终方案选用Android ID和Build信息做种子来生成设备ID，
     * 如果只需要做用户统计等不关心到具体设备的功能，可以只使用Android ID做种子，在获取到的Android ID等于9774d56d682e549c时，就随机生成一个ID代替；
     * 如果要实现推送消息等需要精确区分设备的功能，可以用Android ID和Build的部分设备信息做种子。
     *
     * 还需要注意的是，以上全部标识都可能被用户或者系统修改的，应用每次获取的Android ID或者Device ID等种子可能并不相同，生成的设备ID也会不一样，
     * 为了解决这问题，可以把生成的设备ID保存起来，每次使用时先检查有没有已经保存的设备ID，如果没有才生成一个并保存，
     * 保存的位置可以是应用的私有空间或者公共空间，具体选择视乎是否需要多个应用使用同一个设备ID。
     *
     *
     * @return
     */
    static public String getDeviceId() {
        String id = "";
        if(false) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
                id = tm.getDeviceId();
                if (null == id) {
                    id = tm.getSubscriberId();
                }
            } else {
                id = Settings.Secure.getString(App.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            if (null == id) {
                WifiManager wifiManager = (WifiManager) App.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                id = wifiManager.getConnectionInfo().getMacAddress();
            }
        }else {
            id = getDeviceUUID(App.getContext());
        }
        Log.i("Utils", "getDeviceId: " + id);
        return id;
    }

    static public String getWifiMacAddress() {
        TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) App.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String id = wifiManager.getConnectionInfo().getMacAddress();
        return id;
    }

    /**
     * 获取屏幕分辨率 如320*240
     *
     * @return
     */
    public static String getDeviceScreenResolution() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        String result = dm.widthPixels + "*" + dm.heightPixels;
        return result;
    }

    /**
     * cpu型号
     *
     * @return proc/cpuinfo文件中第一行是CPU的型号， 第二行是CPU的频率，可以通过读文件，读取这些数据！
     */
    public static String getDeviceCPUInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""};
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            if (null != str2) {
                arrayOfString = str2.split("\\s+");
                for (int i = 2; i < arrayOfString.length; i++) {
                    cpuInfo[0] += arrayOfString[i] + " ";
                }
            }
            localBufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = cpuInfo[0].substring(0, cpuInfo[0].indexOf(" "));
        return result;
    }



    /**
     * 当前网路是否可用
     */
    static public boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected() && info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }

        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 获取当前网络类型
     * return -1:不可用; 0:wifi; 1:移动网络
     */
    static public int getCurrentNetWorkType() {
        int ret = -1;
        ConnectivityManager manager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isAvailable() || !info.isConnected()) {
            ret = -1;
        } else if (info.getState() == NetworkInfo.State.CONNECTED) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                ret = 0;
            else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                ret = 1;
        }

        return ret;
    }

    private static Random randGen = null;
    private static char[] numbersAndLetters = ("0123456789ABCDEF").toCharArray();


    /**
     * 生成length长度的由0123456789ABCDEF随机组成的字符串
     *
     * @param length
     * @return
     */
    public static final String randomString(int length) {

        if (length < 1) {
            return null;
        }
        if (randGen == null) {
            randGen = new Random();
        }
        char[] randBuffer = new char[length];
        int numbersAndLettersLength = numbersAndLetters.length;
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(numbersAndLettersLength)];
        }
        return new String(randBuffer);
    }

    /**
     * 全局显示等待框
     * @param context
     * @param msg
     * @return
     */
    private static ProgressDialog progressDialog = null;
    public static void showProgressDialog(Context context, String msg) {
        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    if(progressDialog == null){
                        progressDialog = new ProgressDialog(context);
                    }
                   Log.i("Utils", "progressDialog: " + progressDialog);
                    progressDialog.setMessage(msg);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    if(progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = null;
                }
            }
        };

    }

    public static void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }

        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    progressDialog.dismiss();
                    progressDialog = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    /**
     * 全局显示等待框
     *
     * @param context
     * @param msg
     * @return
     */
    private static ProgressDialog mProgressDialog = null;
    public static void showProgressDialog2(final Context mContext, final String msg) {
        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                        mProgressDialog = new ProgressDialog(mContext, R.style.Dialog2);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                    }

                    int width = Utils.getScreenWidth();//获取界面的宽度像素
                    int height = Utils.getScreenHeight();
                    WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();//一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
                    params.alpha = 0.3f;//设置进度条背景透明度
//                    params.height = height*2/5;//设置进度条的高度
                    params.gravity = Gravity.CENTER;//设置ProgressDialog的中心
//                    params.width = 4*width/5;//设置进度条的宽度
                    params.dimAmount = 0.3f;//设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，和原来的灰度一样
                    mProgressDialog.getWindow().setAttributes(params);//把参数设置给进度条，注意，一定要先show出来才可以再设置，不然就没效果了，因为只有当界面显示出来后才可以获得它的屏幕尺寸及参数等一些信息

                    mProgressDialog.setMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    }

    public static void dismissProgressDialog2() {
        new HandlerPost(0, true){

            @Override
            public void doAction() {
                try {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
            }
        };
    }

    public static Dialog showCustomProgressDialog(final Context context, final String msg, final boolean cancelable){
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progressdialog_layout, null);
        View dialog_view = view.findViewById(R.id.dialog_view);
        ImageView progress_imageView = view.findViewById(R.id.progress_imageView);
        TextView progress_textView = view.findViewById(R.id.progress_textView);
        //加载动画
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.load_animation);
        progress_imageView.startAnimation(animation);
        if(TextUtils.isEmpty(msg)){
            progress_textView.setVisibility(View.GONE);
        }else {
            progress_textView.setVisibility(View.VISIBLE);
            progress_textView.setText(msg);
        }

        Dialog loadingDialog = new Dialog(context, R.style.Dialog2);
        loadingDialog.setCancelable(cancelable);
        loadingDialog.setContentView(dialog_view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        loadingDialog.show();

        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();//一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
        params.alpha = 1.0f;//设置进度条背景透明度
        params.gravity = Gravity.CENTER;//设置ProgressDialog的中心
        params.dimAmount = 0.4f;//设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，和原来的灰度一样
        loadingDialog.getWindow().setAttributes(params);//把参数设置给进度条，注意，一定要先show出来才可以再设置，不然就没效果了，因为只有当界面显示出来后才可以获得它的屏幕尺寸及参数等一些信息

        return loadingDialog;
    }

    public static Dialog showCustomProgressDialog(Context context, String msg) {
        return showCustomProgressDialog(context, msg, false);
    }

    private static Dialog loadingDialog = null;
    private static ProgressBar progressBar = null;
    private static TextView progress_textView = null;

    public static void showCustomProgressBarDialog(Context context, float value) {
        if(loadingDialog == null) {
            loadingDialog = new Dialog(context, R.style.Dialog3);
            loadingDialog.setCancelable(false);
            View view = LayoutInflater.from(context).inflate(R.layout.custom_progressbar_layout, null);
            progressBar = view.findViewById(R.id.progressbar);
            progress_textView = view.findViewById(R.id.progress_textView);
            loadingDialog.setContentView(view, new LinearLayout.LayoutParams(Utils.getScreenWidth()*3/4,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            loadingDialog.show();

            WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();//一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
            params.alpha = 1.0f;//设置进度条背景透明度
            params.gravity = Gravity.CENTER_VERTICAL;//设置ProgressDialog的中心
            params.dimAmount = 0.5f;//设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，和原来的灰度一样
            loadingDialog.getWindow().setAttributes(params);//把参数设置给进度条，注意，一定要先show出来才可以再设置，不然就没效果了，因为只有当界面显示出来后才可以获得它的屏幕尺寸及参数等一些信息
        }

        progressBar.setProgress((int)(value * 100));
        progress_textView.setText((int)(value * 100) + "%");
    }

    public static void dismissCustomProgressBarDialog() {
        if(loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
    /*
    *********************************************************************************************************
    *  CRC校验算法
    * Function Name  :                 int16_t Setget_crc16 ( uint8_t *bufData, uint16_t buflen, uint8_t *pcrc )
    * Description    : TCPÃüÁîCRCÐ£Ñé
    * Input          :
    * Output         :
    * Return         : ÎÞ
    *********************************************************************************************************
    */
    int Setget_crc16(byte[] bufData, int buflen, byte[] pcrc) throws Exception {
        int ret = 0;
        int TCPCRC = 0xffff;
        int POLYNOMIAL = 0xa001;
        byte i, j;

        if (bufData == null || pcrc == null) {
            return -1;
        }

        if (buflen == 0) {
            return ret;
        }

        for (i = 0; i < buflen; i++) {
            TCPCRC ^= bufData[i];
            for (j = 0; j < 8; j++) {
                if ((TCPCRC & 0x0001) != 0) {
                    TCPCRC >>= 1;
                    TCPCRC ^= POLYNOMIAL;
                } else {
                    TCPCRC >>= 1;
                }
            }
        }
//    printf ("TCPCRC=%X\n", TCPCRC);
        pcrc[0] = (byte) (TCPCRC & 0x00ff);
        pcrc[1] = (byte) (TCPCRC >> 8);
        return ret;
    }

    // 拼参数ccbParam
    public static String ccbParam(HashMap<String, String> map) {
        String result = "";
        for (String key : map.keySet()) {
            String tmp;
            if (!"TXN_INF".equals(key)) {
                tmp = "&" + key + "=" + URLEncoder.encode(map.get(key)) ;
            } else {
                tmp = "&" + key + "=" + map.get(key);
            }
            result += tmp;
        }
        return result;
    }

    /**
     * mpc项目中 解析服务端的复杂数据
     * @param inputStream
     * @return 返回一个解析后的简单实用数据
     */
    public static HashMap<String, Object> resolutionMPCDataXML(InputStream inputStream){
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        try {
//			Log.e("xlu", JsonParser.formatStreamToString(inputStream));
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(inputStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("RESP_CODE".equals(parser.getName())) {
                        resultMap.put("respCode", parser.nextText());
                    } else if ("RESP_INFO".equals(parser.getName())) {
                        resultMap.put("respInfo", parser.nextText());
                    } else if ("SIMILARITY".equals(parser.getName())) {
                        resultMap.put("similarity", parser.nextText());
                    }
                }
                eventType = parser.next();
            }


//			String targData = JsonParser.formatStreamToString(inputStream);
//			resultMap = (HashMap<String, Object>)JsonParser.parserRandomJsonFormat(targData);
//			hashMap = (HashMap)((ArrayList)hashMap.get("resultList")).get(0);
//			if(TRADESTATUS.equals(hashMap.get("status"))){//返回正确信息
//				resultMap = (HashMap)hashMap.get("result");//客户端返回的信息
//				resultMap.put("status", hashMap.get("status"));
//			}else if(TRADESATUSFAILRUE.equals(hashMap.get("status"))){//返回错误信息
//				resultMap = (HashMap)hashMap.get("error");
//				resultMap = (HashMap)resultMap.get("errObject");
//				resultMap =(HashMap)((ArrayList)resultMap.get("errorList")).get(0);
//				resultMap.put("status", hashMap.get("status"));
//			}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public static byte[] toByteArray(String hexString) {
        if (null == hexString) {
            throw new IllegalArgumentException("this hexString must not be empty");
        }

        hexString = hexString.toLowerCase().replace(" ", "");
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) );
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) );
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    private final static char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] byteArray) {
        if (byteArray == null) {
            return "";
        }
        return toHexString(byteArray, 0, byteArray.length);
    }

    public static String toHexString(byte[] d, int s, int n) {
        if (d == null) {
            return "";
        }
        final char[] ret = new char[n * 2];
        final int e = s + n;

        int x = 0;
        for (int i = s; i < e; ++i) {
            final byte v = d[i];
            ret[x++] = HEX[0x0F & (v >> 4)];
            ret[x++] = HEX[0x0F & v];
        }
        return new String(ret);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @param digestType MD5, SHA-256, SHA-512
     * @param string
     * @return
     */
    public static String stringToDigest(String digestType, String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance(digestType).digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    /**
     * Uri 转 File
     * @param uri
     * @param context
     * @return
     */
    public static File uriToFile(Uri uri, Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA }, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }


    /**
     * File 转 Uri
     * @param context
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     *   * 获取android当前可用运行内存大小
     *   * @param context
     *   *
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }


    /**
     * 将图片按指定的角度旋转
     * @param src
     * @param degree
     * @return
     */
    public static Bitmap rotateBitmapDegree(Bitmap src, int degree) {

        if(degree % 360 == 0) return src;

        Runtime.getRuntime().gc();

        long bitmapLen = src.getRowBytes() * src.getHeight();
        float fileSize = bitmapLen / 1024 + bitmapLen % 1024 /1024.0f; //K
        Log.i(TAG, String.format("src bitmap len: %d, fileSize: %.2fK", bitmapLen, fileSize));

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap newBitmap = null;
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            newBitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        if (newBitmap == null) {
            newBitmap = src;
        }
        if (src != newBitmap) {
            src.recycle();
        }
        return newBitmap;
    }

    /**
     * 手机号判断运营商正则
     * @param mobileStr
     * @return 1:电信 2:联通 3：移动
     */
	  /**
     * 中国电信号码正则
     * 133、141、149、153、173、174、177、179、180、181、189、190、191、193、199
     * 虚拟运营商号段: 162、1700、1701、1702
       *
       * 中国联通号码正则
       * 130、131、132、140、145、146、155、156、166、175、176、185、186、196
       * 虚拟运营商号段: 167、1704、1707、1708、1709、171
       *
       * 中国移动号码正则
       * 134、135、136、137、138、139、144、147、148、150、151、152、157、158、159、172、178、182、183、184、187、188、195、197、198
       * 虚拟运营商号段: 165、1703、1705、1706
     **/
    public static int getMobileName(String mobileStr){
        Pattern pattern1 = Pattern.compile("(^1(33|4[1,9]|53|62|7[3,4,7,9]|8[0,1,9]|9[0,1,3,9])\\d{8}$)|(^170[0-2]\\d{7}$)");
        Pattern pattern2 = Pattern.compile("(^1(3[0-2]|4[0,5,6]|5[5,6]|6[6,7]|7[1,5,6]|8[5,6]|96)\\d{8}$)|(^170[4,7-9]\\d{7}$)");
        Pattern pattern3 = Pattern.compile("(^1(3[4-9]|4[4,7,8]|5[0-2,7-9]|65|7[2|,8]|8[2-4,7-8]|9[5,7,8])\\d{8}$)|(^170[3,5,6]\\d{7}$)");


        Matcher mather =  pattern1.matcher(mobileStr);
        if(mather.find()){
            return 1;
        }
        mather =  pattern2.matcher(mobileStr);
        if(mather.find()){
            return 2;
        }

        mather =  pattern3.matcher(mobileStr);
        if(mather.find()){
            return 3;
        }
        return 0;
    }

    /**
     * 手机号正则
     */
    public static boolean isMobile(String mobileStr){
        Pattern pattern = Pattern.compile("^1(3[0-9]|4[5-9]|5[0-35-9]|66|7[0-35-8]|8[0-9]|9[1,8,9])\\d{8}$");
        Matcher mather =  pattern.matcher(mobileStr);
        if(mather.find()){
            return true;
        }
        return false;
    }

    ///
    /**
     * 非精确的正则表达式：验证手机号格式
     */
    private static final String MOBILE_PATTERN = "^[1][3-9][0-9]{9}$";

    /**
     * 中国移动号码正则
     * 134、135、136、137、138、139、144、147、148、150、151、152、157、158、159、172、178、182、183、184、187、188、195、197、198
     * 虚拟运营商号段: 165、1703、1705、1706
     **/
    private static final String CMCC_PATTERN = "(^1(3[4-9]|4[478]|5[0-27-9]|65|7[28]|8[2-478]|9[578])\\d{8}$)|(^170[356]\\d{7}$)";

    /**
     * 中国电信号码正则
     * 133、141、149、153、173、174、177、179、180、181、189、190、191、193、199
     * 虚拟运营商号段: 162、1700、1701、1702
     **/
    private static final String TELECOM_PATTERN = "(^1(33|4[19]|53|62|7[3479]|8[019]|9[0139])\\d{8}$)|(^170[012]\\d{7}$)";

    /**
     * 中国联通号码正则
     * 130、131、132、140、145、146、155、156、166、175、176、185、186、196
     * 虚拟运营商号段: 167、1704、1707、1708、1709、171
     **/
    private static final String UNICOM_PATTERN = "(^1(3[0-2]|4[056]|5[56]|6[67]|7[156]|8[56]|96)\\d{8}$)|(^170[4789]\\d{7}$)";

    //移动
    private static final String CMCC = "中国移动";
    //电信
    private static final String TELECOM = "中国电信";
    //联通
    private static final String UNICOM = "中国联通";

    private static String UNKNOWN = "未知";


    /**
     * 非精确的手机规则判断
     * @param mobile
     * @return
     */
    public static boolean isMobileByNotExactly(String mobile){
        return Pattern.matches( "^[1][3-9][0-9]{9}$", mobile);
    }

    /**
     * 粗放的校验手机号格式是否正确
     *
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean checkMobile(String mobile) {

        if(checkPhoneNumber(mobile)) {
            return true;
        }
        return Pattern.matches(MOBILE_PATTERN, mobile);
    }

    /**
     * 检查手机号格式是否满足中国电信、中国移动、中国联通的号段
     * @param phone
     * @return
     */
    public static boolean checkPhoneNumber(String phone) {
        if (Pattern.matches(TELECOM_PATTERN, phone)) {
            return true;
        }

        if (Pattern.matches(CMCC_PATTERN, phone)) {
            return true;
        }

        if (Pattern.matches(UNICOM_PATTERN, phone)) {
            return true;
        }

        return false;
    }

    /**
     * 检查手机号所属的运营商
     * @param phone
     * @return
     */
    public static String checkCarrier(String phone) {

        if (Pattern.matches(TELECOM_PATTERN, phone)) {
            return TELECOM;
        }

        if (Pattern.matches(CMCC_PATTERN, phone)) {
            return CMCC;
        }

        if (Pattern.matches(UNICOM_PATTERN, phone)) {
            return UNICOM;
        }

        return UNKNOWN;
    }

    /**
     * 检查手机号所属的运营商编号
     * @param phone
     * @return 0：未知，  1：中国电信，  2：中国移动，  3：中国联通
     */
    public static int checkCarrierNo(String phone) {

        if (Pattern.matches(TELECOM_PATTERN, phone)) {
            return 1;
        }

        if (Pattern.matches(CMCC_PATTERN, phone)) {
            return 2;
        }

        if (Pattern.matches(UNICOM_PATTERN, phone)) {
            return 3;
        }

        return 0;
    }

    //////////

    public static boolean isIDNumber(String str){
        Pattern pattern = Pattern.compile("^(\\d{17})[xX,\\d]$");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find())
            return true;

        return false;
    }

    public static boolean isValidIMAccount(String account){
        return isValid(account);
    }

    public static boolean isValidPassword(String pwd){
       return isValid(pwd);
    }

    public static boolean isValid(String str) {
        Pattern pattern1 = Pattern.compile("^[0-9a-zA-Z]{8,16}$");//8-16位字母、数字的组合
        Pattern pattern2 = Pattern.compile("[\\d]+"); //至少有1位数字
        Pattern pattern3 = Pattern.compile("[a-zA-Z]+"); //至少有1位字母

        Matcher mather1 =  pattern1.matcher(str);
        Matcher mather2 =  pattern2.matcher(str);
        Matcher mather3 =  pattern3.matcher(str);

        if(mather1.find() && mather2.find() && mather3.find()){
            return true;
        }
        return false;
    }

    public static boolean isValidName(String name) {
        Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5]{2,}$");//至少两位汉字
        Matcher matcher = pattern.matcher(name);
        if(matcher.find()) {
            return true;
        }
        return false;
    }

    public static String getSumOfHanzi(String name) {
        String output = "";
         String lastName = "";
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher matcher = p.matcher(name);
        while (matcher.find()) {
            lastName = matcher.group(0);
            output += "*";
        }

        if(TextUtils.isEmpty(output)){
            output = name;
        }else {
            output = output.substring(0, output.lastIndexOf("*")) + lastName;
        }

        return output;
    }

    public static boolean isValidBankCardNo(String cardNo) {
        Pattern pattern = Pattern.compile("^[1-9]{1}\\d{15,18}$");//首位非0， 16^19位
        Matcher matcher = pattern.matcher(cardNo);
        if(matcher.find()) {
            return true;
        }
        return false;
    }





    /**
     * 存放拍摄图片的文件夹
     */
    private static final String FILES_NAME = "/MyPhoto";
    /**
     * 获取的时间格式
     */
    public static final String TIME_STYLE = "yyyyMMddHHmmss";
    /**
     * 图片种类
     */
    public static final String IMAGE_TYPE = ".png";


    /**
     * 获取手机可存储路径
     *
     * @param context 上下文
     * @return 手机可存储路径
     */
    private static String getPhoneRootPath(Context context) {
        // 是否有SD卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            // 获取SD卡根目录
            return context.getExternalCacheDir().getPath();
        } else {
            // 获取apk包下的缓存路径
            return context.getCacheDir().getPath();
        }
    }

    /**
     * 使用当前系统时间作为上传图片的名称
     *
     * @return 存储的根路径+图片名称
     */
    public static String getPhotoFileName(Context context) {
        File file = new File(getPhoneRootPath(context) + FILES_NAME);
        // 判断文件是否已经存在，不存在则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 设置图片文件名称
        SimpleDateFormat format = new SimpleDateFormat(TIME_STYLE, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = format.format(date);
        String photoName = "/" + time + IMAGE_TYPE;
        return file + photoName;
    }

    /**
     * 保存Bitmap图片在SD卡中
     * 如果没有SD卡则存在手机中
     *
     * @param mbitmap 需要保存的Bitmap图片
     * @return 保存成功时返回图片的路径，失败时返回null
     */
    public static String savePhotoToSD(Bitmap mbitmap, Context context) {
        FileOutputStream outStream = null;
        String fileName = getPhotoFileName(context);
        try {
            outStream = new FileOutputStream(fileName);
            // 把数据写入文件，100表示不压缩
            mbitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outStream != null) {
                    // 记得要关闭流！
                    outStream.close();
                }
                if (mbitmap != null) {
                    mbitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把原图按1/10的比例压缩
     *
     * @param path 原图的路径
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 10;  // 图片的大小设置为原来的十分之一
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        options = null;
        return bmp;
    }

    public static Bitmap getCompressPhoto(String path, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;  // 图片的大小设置为原来的几分之一
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        options = null;
        return bmp;
    }

    /**
     * 处理旋转后的图片
     * @param originpath 原图路径
     * @param context 上下文
     * @return 返回修复完毕后的图片路径
     */
    public static String amendRotatePhoto(String originpath, Context context) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originpath);

        // 把原图压缩后得到Bitmap对象
        Bitmap bmp = getCompressPhoto(originpath);;

        // 修复图片被旋转的角度
        Bitmap bitmap = rotaingImageView(angle, bmp);

        // 保存修复后的图片并返回保存后的图片路径
        return savePhotoToSD(bitmap, context);
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     * @param angle 被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    public static final boolean DEBUG = false;
    public static void i(String tag, String msg){
        if(DEBUG){
            android.util.Log.i(tag,msg);
        }
    }

}
