package com.milanac007.demo.im.utils;

import java.util.List;

import hanyu.pinyin.HanyuPinyinHelper;

public class PinYin {
    private static HanyuPinyinHelper helper = null;
    
    public static List<String> getPinYin(String str){
        if(helper == null){
            synchronized (PinYin.class){
                if(helper == null){
                    helper = new HanyuPinyinHelper();
                }
            }
        }

        return helper.hanyuPinYinConvert(str);
    }
}
