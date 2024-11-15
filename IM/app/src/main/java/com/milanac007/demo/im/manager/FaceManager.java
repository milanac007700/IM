package com.milanac007.demo.im.manager;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zqguo on 2016/11/14.
 */
public class FaceManager {
    public static final int FACE_NUM = 53;
    public static final int COUNT_PER_PAGE = 20;

    private String FaceStr = "[微笑]@face000 [NO]@face001 [OK]@face002 [爱你]@face003 [爱心]@face004 [傲慢]@face005 [白眼]@face006 [棒棒糖]@face007 " +
                 "[抱抱]@face008 [抱拳]@face009 [爆筋]@face010 [鄙视]@face011 [闭嘴]@face012 [鞭炮]@face013 [便便]@face014 [擦汗]@face015 [彩带]@face016 " +
                "[彩球]@face017 [菜刀]@face018 [差劲]@face019 [钞票]@face020 [车厢]@face021 [打哈欠]@face022 [大兵]@face023 [大哭]@face024 "+
                "[蛋糕]@face025 [得意]@face026 [害羞]@face027[憨笑]@face028 [惊讶]@face029 [可爱]@face030 [可怜]@face031 [流汗]@face032 " +
                "[流泪]@face033 [难过]@face034 [敲打]@face035 [色]@face036  [龇牙]@face037 [偷笑]@face038 [吐]@face039 [嘘]@face040 " +
                "[疑问]@face041 [阴险]@face042 [左哼哼]@face043 [右哼哼]@face044 [晕]@face045 [调皮]@face046 [胜利]@face047 [示爱]@face048 " +
                "[抓狂]@face049 [猪头]@face050  [咒骂]@face051 [再见]@face052 ";

    public String find(String faceId) {
        Pattern pattern = Pattern.compile(String.format("(\\[[\\u4e00-\\u9fa5, OK, NO]{1,3}\\])@%s", faceId));
        Matcher mather =  pattern.matcher(FaceStr);
        if(mather.find()){
            return mather.group(1);
        }
        return null;
    }

    /**
     * 用于查找[开头、]结尾的字符串是否为合法Face
     * @param faceStr
     * @return 找到返回face字符串长度；否则-1
     */
    public int findLength(String faceStr) {
        String tmpStr = faceStr.replace("[", "\\[").replace("]", "\\]");
        Pattern pattern = Pattern.compile(String.format("(%s)@face[0-9]{3}", tmpStr));
        Matcher mather =  pattern.matcher(FaceStr);
        if(mather.find()){
            return mather.group(1).length();
        }
        return -1;
    }

    public String replaceFaceChacter(String faceContent){
        String temp = faceContent;
        Pattern pattern = Pattern.compile("\\[[\\u4e00-\\u9fa5, OK, NO]{1,3}\\]");
        Matcher mather =  pattern.matcher(temp);
        while (mather.find()){
            String oldStr= mather.group();
            String newStr = findPicName(oldStr);
            if(!TextUtils.isEmpty(newStr)) {
                temp = temp.replace(oldStr, newStr);
            }
        }

        return temp;
    }

    public String findPicName(String faceContent) {
        faceContent = faceContent.replace("[", "\\[").replace("]", "\\]");
        Pattern pattern = Pattern.compile(String.format("%s@(face[0-9]{3})", faceContent));
        Matcher mather =  pattern.matcher(FaceStr);
        if(mather.find()){
            return "<img src=\"faceimages/" + mather.group(1) + ".png\" style=\"display:block;width:200%;\" />";
        }
        return null;
    }

    static private FaceManager instance = null;
    static public FaceManager getInstance(){
        if(instance == null){
            synchronized (FaceManager.class){
                if(instance == null){
                    instance = new FaceManager();
                }
            }
        }

        return instance;
    }

    public FaceManager(){

    }

}
