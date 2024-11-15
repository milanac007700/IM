-optimizationpasses 5

#混淆后的类名小写
-dontusemixedcaseclassnames

##不跳过非公共的库的类和类成员
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

#把混淆类中的方法名也混淆了
-useuniqueclassmembernames

#关闭预验证
-dontpreverify

#打印过程日志
-verbose

#-dontshrink #禁用压缩

#指定优化算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#关闭优化
-dontoptimize

#扩大类和类成员的访问权限，使优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification


#如果您使用的是Google的可选许可证验证库，则可以将其代码与自己的代码混淆。 您必须保留其ILicensingService接口以使库正常工作：
-keep public interface com.android.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingallowaccessmodificationService

#将崩溃日志文件来源重命名为“SourceFile”
-renamesourcefileattribute SourceFile
#保护注解
-keepattributes *Annotation*
#不混淆泛型
-keepattributes Signature


#保留文件名和行号
-keepattributes SourceFile,LineNumberTable

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

#使ProGuard知道该库引用了并非所有版本的API都可用的某些类
-dontwarn android.support.**
-dontwarn androidx.core.**

#jni方法
-keepclasseswithmembernames class * {
    native <methods>;
}

#AndroidMainfest中的类不混淆，所以四大组件和Application的子类和Framework层下所有的类默认不会进行混淆。
#自定义的View默认也不会被混淆；所以像网上贴的很多排除自定义View，或四大组件被混淆的规则在Android Studio中是无需加入的；
#链接：https://www.jianshu.com/p/7436a1a32891

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#
#
##Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
#-keep public class * extends android.support.v4.app.Fragment
#-keep public class * extends android.app.Fragment
#-keep public class * extends androidx.app.Fragment
#
##保持自定义View的get和set相关方法
#-keepclassmembers public class * extends android.view.View {
#   void set*(***);
#   *** get*();
#}
#
##保持Activity中View及其子类入参的方法
#-keepclassmembers class * extends android.app.Activity {
#   public void *(android.view.View);
#}
#
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}

#R文件的静态成员
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keeppackagenames com.bjxz.srhy.*
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField *; # 仅忽略混淆使用了DatabaseField注解的类成员
}

#fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*; }

#ormlite db
-dontwarn com.j256.ormlite.**
-keep class com.j256.ormlite.** {*;}

#活体
-dontwarn com.sensetime.**
-keep class com.sensetime.** { *; }

#rxjava
-dontwarn io.reactivex.rxjava3.**
-keep class io.reactivex.rxjava3.** {*;}

#高德定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.loc.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

#eventBus
-keepclassmembers class ** {
    public void onEventMainThread*(**);
    public void onEvent*(**);
    public void onEventBackgroundThread*(**);
    public void onEventAsync*(**);
}
-keep enum org.greenrobot.eventbus.SubscribeProguardKeepRules

-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

