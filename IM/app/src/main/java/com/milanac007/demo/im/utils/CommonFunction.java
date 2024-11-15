package com.milanac007.demo.im.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.milanac007.pickerandpreviewphoto.MyApplication;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.R;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.PeerEntity;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.logger.Logger;
import com.example.milanac007.pickerandpreviewphoto.CacheManager;
import com.milanac007.demo.im.net.NetConstants;
import com.milanac007.demo.im.ui.CircleImageView;
import com.milanac007.demo.im.ui.GlideImageView;
import com.milanac007.demo.im.utils.pinyin.PinYin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by zqguo on 2016/9/14.
 */
public class
CommonFunction {

    public static final String APPLICATION_PACKAGE_TAG = App.getContext().getPackageName();
    private static Logger logger = Logger.getLogger();


    /**显示加载的动画
     * @param show
     * @param loading
     * @param loadingAnim
     */
    public static void showLoadingAnim(boolean show, ImageView loading, Animation loadingAnim){
        if (show) {
            loading.setVisibility(View.VISIBLE);
            loading.startAnimation(loadingAnim);
        } else {
            loading.setVisibility(View.INVISIBLE);
            loading.clearAnimation();
        }
    }

    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     *
     * @param inputString
     * @return
     */
    public static String getStringPingYin(String inputString) {

        String output = "";
//        if(inputString == null) {
//            return output;
//        }
//
//        try {
//            List<String> list = PinYin.getPinYin(inputString);
//            if(list == null || list.isEmpty())
//                return output;
//
//            if(list.size() == 1){
//                output += list.get(0);
//            }else {
//                output += list.get(1);
//            }
//
//        }catch (ConcurrentModificationException e){
//            e.printStackTrace();
//        }

        PinYin.PinYinElement element = new PinYin.PinYinElement();
        PinYin.getPinYin(inputString, element);
        output = element.pinyin;
       return output;
    }

    

    public static void setHeadIconImageView(GlideImageView view, PeerEntity peerEntity) {

        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException(String.format("can not visit the method from UI thread: ", CommonFunction.class.getName() + "#" + "setHeadIconImageView"));
        }

        if (view == null || peerEntity == null){
            return;
        }

        String path = peerEntity.getAvatarLocalPath();
        if(!TextUtils.isEmpty(path)){
            final Bitmap bitmap = CacheManager.getInstance().getBitmapFormCache(path);
            if(bitmap != null){
                new HandlerPost(0, true){
                    @Override
                    public void doAction() {
                        view.setImageBitmap(bitmap);
                    }
                };
                return;
            }else if(new File(path).exists()){
                final Bitmap bitmap2 = CacheManager.getInstance().addCacheData(path, 100, 100);
                new HandlerPost(0, true){
                    @Override
                    public void doAction() {
                        view.setImageBitmap(bitmap2);
                    }
                };
                return;
            }
        }

        if(peerEntity instanceof UserEntity){
            if(view instanceof CircleImageView){
                CircleImageView cImgView = (CircleImageView) view;
                UserEntity person = (UserEntity)peerEntity;
                switch (person.getGender()) {
                    default:
                    case 0:
                        cImgView.setGenderBackground(R.drawable.default_head_icon_bg_male);
                        break;
                    case 1:
                        cImgView.setGenderBackground(R.drawable.default_head_icon_bg_female);
                        break;
                }
                String name = person.getMainName();
                if (!isStringEmpty(name) && (name.length() >= 1)) {
                    cImgView.setText(name.substring(name.length()-1));
                }else {
                    String userCode = person.getUserCode();
                    if (!isStringEmpty(userCode) && (userCode.length() >= 1)) {
                        cImgView.setText(userCode.substring(userCode.length() - 1));
                    }
                }
            }
        }else if(peerEntity instanceof GroupEntity){
            GroupEntity group = (GroupEntity)peerEntity;
            view.setImageResource(R.mipmap.group_default);
        }

        if (!TextUtils.isEmpty(peerEntity.getAvatar())) {
            String iconUrl = NetConstants.FILE_SERVER_URL + peerEntity.getAvatar();
            if (URLUtil.isValidUrl(iconUrl)) {
                view.setImageBitmap(iconUrl, peerEntity); //耗时，异步加载
            }
        }

    }

    public static boolean isStringEmpty(Object obj) {
		boolean ret = false;

		if (obj == null || obj.toString().trim().isEmpty() || obj.toString().length() == 0) {
			ret = true;
		}
		return ret;
	}
    
    
    private static float scale = 0.0f; // 密度
    private static int widthPixels = 0;
    private static int heightPixels = 0;

    public static void init() {
        DisplayMetrics displaysMetrics = new DisplayMetrics();// 初始化一个结构
        WindowManager wm = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaysMetrics);// 对该结构赋值
        widthPixels = displaysMetrics.widthPixels;
        heightPixels = displaysMetrics.heightPixels;
        DisplayMetrics dm = App.getContext().getResources().getDisplayMetrics();
        scale = dm.density;
	}
    
    /**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(float dpValue) {
		if (scale == 0) {
			init();
		}
		return (int) (dpValue * scale + 0.5f);
	}

    /**
     * 得到的屏幕的宽度
     */
    public static int getWidthPx() {
        // DisplayMetrics 一个描述普通显示信息的结构，例如显示大小、密度、字体尺寸
        if (widthPixels == 0) {
            init();
        }
        return widthPixels;
    }

    /**
     * 得到的屏幕的高度
     */
    public static int getHeightPx() {
        // DisplayMetrics 一个描述普通显示信息的结构，例如显示大小、密度、字体尺寸
        if (heightPixels == 0) {
            init();
        }
        return heightPixels;
    }


    private static String SDDir = null;

    public static String getRootDir() {
        if (TextUtils.isEmpty(SDDir)) {
            initExtStorageDir();
        }
        return SDDir;
    }

    protected static void initExtStorageDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //TODO Android10 作用域存储
//                SDDir = Environment.getExternalStorageDirectory().getAbsolutePath(); //java.io.FileNotFoundException: /storage/emulated/0/CustomCamera/...: open failed: ENOENT (No such file or directory)
//                SDDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                SDDir = MyApplication.getContext().getExternalFilesDir(null).getAbsolutePath();
            } else {
                SDDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
    }

    private static String makeDirPath(String folder, String userUuid) {
        String dir = getRootDir() + File.separator;

        if (!TextUtils.isEmpty(userUuid)) {
            dir += userUuid;
            dir += File.separator;
        }

        dir += folder;
        dir += File.separator;

        File f = new File(dir);
        if (!f.exists()) {
            boolean ret = f.mkdirs();
            Log.i(APPLICATION_PACKAGE_TAG, "makedir ret=" + ret + dir);
        }

        return dir;
    }


    public static String getUserDBPath() {
        return makeDirPath("db", "");
    }

    public static String getDirUserTemp() {
        return makeDirPath("temp", "");
    }

    // 示例：http://file01.yugusoft.com/M00/00/7D/OkTuVVOwxrOAHsffAAAiIWbby2Q947.jpg
    // 根据url 截取文件名字 为OkTuVVOwxrOAHsffAAAiIWbby2Q947.jpg.
    public static String getImageFileNameByUrl(String imgUrl) {
        String[] strArr = imgUrl.split("/");

        if (strArr.length > 0) {
            String temp = strArr[strArr.length - 1];
            if (temp.contains(".")) {
                return temp;
            }
        }

        return imgUrl;
    }

    /**
     * 获取文件后缀名
     * @param file
     * @return
     */
    public static String getSuffix(File file){
        if(!file.exists())
            return null;

        String fileName=file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        return suffix;
    }

    public static String getSuffix(String fileName){
        if(TextUtils.isEmpty(fileName))
            return null;

        String suffix=fileName.substring(fileName.lastIndexOf(".")+1);
        return suffix;
    }

    private static Toast mToast = null;

    public static Toast getToast(){
        if(mToast == null){
            mToast = Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT);
        }else {
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        return mToast;
    }

    public static Toast getToast(String strId){
        getToast().setText(strId);
        return mToast;
    }

    public static Toast getToast(int msg){
        getToast().setText(msg);
        return mToast;
    }

    public static final void showToast(final int strId) {
        logger.e("showToast(final int strId): Looper.getMainLooper().getThread().getId() = %d, Thread.currentThread().getId() = %d",
                Looper.getMainLooper().getThread().getId(), Thread.currentThread().getId());

        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            new HandlerPost(0, true){
                @Override
                public void doAction() {
                    getToast(strId).show();
                }
            };
        }else {
            getToast(strId).show();
        }
    }

    public static final void showToast(final String msg) {

        logger.d("showToast(final String msg): Looper.getMainLooper().getThread().getId() = %d, Thread.currentThread().getId() = %d",
                Looper.getMainLooper().getThread().getId(), Thread.currentThread().getId());

        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            new HandlerPost(0, true){
                @Override
                public void doAction() {
                    getToast(msg).show();
                }
            };
        }else {
            getToast(msg).show();
        }
    }

    public static final void hideToast(){
        if(mToast != null){
            mToast.cancel();
            mToast = null;
        }
    }


    //获取时位值
    public static int getHourOfDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("HH", Locale.getDefault());
        String hourStr= format.format(date);
        if(!isStringEmpty(hourStr)) {
            try {
                return Integer.parseInt(hourStr);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            return -1;
        }

        return -1;
    }

    //获取年份
    public static int getYearOfDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.getDefault());
        String hourStr= format.format(date);
        if(!isStringEmpty(hourStr)) {
            try {
                return Integer.parseInt(hourStr);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            return -1;
        }

        return -1;
    }

    public static boolean isCurrentYear(Date cmpDate){
        int cmpYear = getYearOfDate(cmpDate);
        int curYear = getYearOfDate(new Date());

        return cmpYear == curYear;
    }

    //return true if the supplied when is today else false
    public static boolean isToday(long when){
        Time time = new Time();
        time.set(when);

        int whenYear = time.year;
        int whenMonth = time.month;
        int whenMonthDay = time.monthDay;

        time.set(System.currentTimeMillis());

        return (whenYear == time.year) && (whenMonth == time.month) && (whenMonthDay == time.monthDay);
    }

    /**
     * Truncates a date to the date part alone.
     */
    @SuppressWarnings("deprecation")
    public static Date truncateToDate(Date d) {
        if (d instanceof java.sql.Date) {
            return d; // java.sql.Date is already truncated to date. And raises
            // an
            // Exception if we try to set hours, minutes or seconds.
        }

        d = (Date) d.clone();
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        d.setTime(((d.getTime() / 1000) * 1000));
        return d;
    }

    /**
     * Returns the number of days between two dates. The time part of the days
     * is ignored in this calculation, so 2007-01-01 13:00 and 2007-01-02 05:00
     * have one day inbetween.
     */
    private final static long SECONDS_PER_DAY = 24 * 60 * 60;
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String date2Str(Date d, String format){
        if(d == null)
            return "";

        if(isStringEmpty(format))
            format = FORMAT;

        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        return df.format(d);
    }


    /**
     *
     * @param firstDate 起始时间
     * @param secondDate 终止时间
     * @return
     */
    public static long daysBetween(Date firstDate, Date secondDate) {
        // We only use the date part of the given dates
        long firstSeconds = truncateToDate(firstDate).getTime() / 1000;
        long secondSeconds = truncateToDate(secondDate).getTime() / 1000;
        // Just taking the difference of the millis.
        // These will not be exactly multiples of 24*60*60, since there
        // might be daylight saving time somewhere inbetween. However, we can
        // say that by adding a half day and rounding down afterwards, we always
        // get the full days.
        long difference = secondSeconds - firstSeconds;
        // Adding half a day
        if (difference >= 0) {
            difference += SECONDS_PER_DAY / 2; // plus half a day in seconds
        } else {
            difference -= SECONDS_PER_DAY / 2; // minus half a day in seconds
        }
        // Rounding down to days
        difference /= SECONDS_PER_DAY;

        return difference;
    }


    public static String getDisplayTimeFormat(Date mDate){
        String ret = "";
        if(mDate == null)
            return ret;

        Date curDate = new Date();
        int curHour = getHourOfDate(curDate);

        if(isToday(mDate.getTime())){ //今天
            if(curHour >=0 && curHour <=6){
                ret = date2Str(mDate, "凌晨 HH:mm");
            }else if(curHour > 6 && curHour <= 11){
                ret = date2Str(mDate, "上午 HH:mm");
            }else if(curHour > 11 && curHour <= 17){
                ret = date2Str(mDate, "下午 HH:mm");
            }else if(curHour >17 && curHour <=24){
                ret = date2Str(mDate, "晚上 HH:mm");
            }
        }else if(daysBetween(curDate, mDate) == -1){ //昨天
            ret = date2Str(mDate, "昨天 HH:mm");
        }else { //昨天之前
            if(isCurrentYear(mDate)){
                ret = date2Str(mDate, "MM-dd HH:mm");
            }else {
                ret = date2Str(mDate, "yyyy-MM-dd HH:mm");
            }
        }

        return ret;
    }

    /**
     时间显示为
     当天：几分钟前（3分钟前）、几小时前（4小时前）
     昨天：昨天 几点几分（昨天 22:02）
     前天以及更前：几月几号 几点几分（9月19日 22:02）
     去年或者前几年：那年 几月几号（2015年 9月19日）
     @return
     */
    public static String getDisplayTime(Date mDate){
        String ret = "";
        if(mDate == null)
            return ret;

        Date curDate = new Date();

        if(isToday(mDate.getTime())){ //今天
            int mDateTime = (int)(mDate.getTime()/1000);
            int nowTime = (int)(System.currentTimeMillis()/1000);
            int second = nowTime - mDateTime;

            int hour = second/3600;
            int minute = (second % 3600)/60;
            second = second % 60;
            if(hour > 0)
                ret = String.format("%d小时前", hour);
            else if(minute > 0)
                ret = String.format("%d分钟前", minute);
            else if(second >= 0)
//                ret = String.format("%d秒前", second);
                ret = "刚刚";

        }else if(daysBetween(curDate, mDate) == -1){ //昨天
            ret = date2Str(mDate, "昨天 HH点mm分");
        }else if(isCurrentYear(mDate)){ //前天 到 今年
            ret = date2Str(mDate, "MM月dd日 HH点mm分");
        }else { //去年或者前几年
            ret = date2Str(mDate, "yyyy年MM月dd日");
        }

        return ret;
    }

    public static boolean isYestoday(Date mDate){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH)-1);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        long value1 =  c.getTime().getTime(); //前一天的23:59:59

        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH)-1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        long value2 =  c.getTime().getTime();//前一天的0:0:0

        long mValue = mDate.getTime();
        if(mValue >= value1 && mValue <= value2)
            return true;
        else
            return false;

    }

    public static String getCallRecordDisplayTimeFormat(Date mDate){
        String ret = "";
        if(mDate == null)
            return ret;

        Date curDate = new Date();
        if(isToday(mDate.getTime())){ //今天
            ret = date2Str(mDate, "HH:mm");
        }else
        if(daysBetween(curDate, mDate) == -1){ //昨天
//        if(isYestoday(mDate)){ //昨天
            ret = date2Str(mDate, "昨天 HH:mm");
        }else { //昨天之前
            if(isCurrentYear(mDate)){
                ret = date2Str(mDate, "MM-dd HH:mm");
            }else {
                ret = date2Str(mDate, "yyyy-MM-dd HH:mm");
            }
        }

        return ret;
    }

    /**
     * 手机号正则
     */
    public static boolean isMobile(String mobileStr){
        Pattern pattern = Pattern.compile("^1(3[0-9]|4[57]|5[0-35-9]|8[0-9]|70)\\d{8}$");
        Matcher mather =  pattern.matcher(mobileStr);
        if(mather.find()){
            return true;
        }
        return false;
    }

    /**
     * IM账号
     * @param searchStr
     * @return
     */

    public static boolean isIMAccount(String searchStr){
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Pattern pattern1 = Pattern.compile("[\\d]+");
        Matcher mather =  pattern.matcher(searchStr);
        Matcher matcher1 = pattern1.matcher(searchStr);
        if(searchStr.length() >= 8 && searchStr.length() <=16 && mather.find() && matcher1.find()){
            return true;
        }

        return false;
    }

    /**
     * 邮箱
     * @param searchStr
     * @return
     */
    public static boolean isEmail(String searchStr){
        Pattern pattern = Pattern.compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
        Matcher mather =  pattern.matcher(searchStr);

        if(mather.find()){
            return true;
        }
        return false;
    }


    public static boolean isPasswordLegal(String mPassword){

        if(mPassword== null || mPassword.isEmpty()){
            return false;
        }

        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Pattern pattern1 = Pattern.compile("[\\d]+");
        Pattern pattern2 = Pattern.compile("[0-9A-Za-z]{8,16}");

        Matcher mather =  pattern.matcher(mPassword);
        Matcher matcher1 = pattern1.matcher(mPassword);
        Matcher matcher2 = pattern2.matcher(mPassword);

        if(mather.find() && matcher1.find() &&  matcher2.find()){
            return true;
        }

        return false;
    }

    public static String getNameFromUrl(String url){
        String[] strs = url.split("/");
        if(strs == null || strs.length ==0)
            return null;
        return strs[strs.length-1];
    }


    /**
     * 获取视频第一帧的缩略图
     * @param filePath
     * @return
     */
    public static Bitmap getVideoThumbnail(String filePath){
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (RuntimeException e){
            e.printStackTrace();
        }finally {
            try{
                retriever.release();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public static String formatFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0B";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }

        return fileSizeString;
    }

    public static String getVersion(){
        try {
            PackageManager manager = App.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(App.getContext().getPackageName(), 0);
            return info.versionName;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int getVersionCode(){
        try {
            PackageManager manager = App.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(App.getContext().getPackageName(), 0);
            return info.versionCode;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static Object invoke(Object owner, String methodName, Object[] args) throws Exception {

        Class<?> ownerClass = owner.getClass();

        Class<?>[] argsClass = new Class<?>[args.length];

        for (int i = 0, len = args.length; i < len; i++) {
            argsClass[i] = args[i].getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(owner, args);
    }

    /**
     * 全局显示等待框
     * @param context
     * @param msg
     * @return
     */
    private static ProgressDialog progressDialog = null;
    public static void showProgressDialog(Context context, String msg) {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(context);
        }

        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        try {
            progressDialog.dismiss();
            progressDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri ) {

        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.MediaColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.MediaColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private static String hexString="0123456789ABCDEF";
    /*
    * 将字符串编码成16进制数字,适用于所有字符（包括中文）
    */
    public static String encode(String str) {
        if(TextUtils.isEmpty(str))
            return "";

        // 根据默认编码获取字节数组
        byte[] bytes=str.getBytes();
        StringBuilder sb=new StringBuilder(bytes.length*2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for(int i=0;i<bytes.length;i++)
        {
            sb.append(hexString.charAt((bytes[i]&0xf0)>>4));
            sb.append(hexString.charAt((bytes[i]&0x0f)>>0));
        }
        return sb.toString();
    }

    /*
    * 将16进制数字解码成字符串,适用于所有字符（包括中文）
    */
    public static String decode(String bytes)
    {
        try {
            ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
            // 将每2位16进制整数组装成一个字节
            for(int i=0;i<bytes.length();i+=2)
                baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
            return new String(baos.toByteArray());
        }catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 字符串转换unicode
     */
    public static String string2Unicode(String string) {

        StringBuffer unicode = new StringBuffer();

        for (int i = 0; i < string.length(); i++) {

            // 取出每一个字符
            char c = string.charAt(i);

            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }
}
