package com.milanac007.demo.im.enums;

/**
 * Created by zqguo on 2016/9/14.
 */
/**
 * @ClassName: EGender
 * @Description: TODO(性别枚举值)
 */
public enum EGender {
    UNKNOWN(0){
        public String getValueDisplay()
        {
            return "未知";
        }
    },
    FEMALE(1){
        public String getValueDisplay()
        {
            return "女";
        }
    },
    MALE(2){
        public String getValueDisplay()
        {
            return "男";
        }
    };

    private final int mValue;
    EGender(int value) {
        this.mValue = value;
    }

    public static EGender valueOf(int value)
    {
        EGender ret = UNKNOWN;
        switch (value) {
            case 1:
                ret = FEMALE;
                break;
            case 2:
                ret = MALE;
            default:
                break;
        }
        return ret;
    }
    /**
     * 获取性别的 int值
     * @return
     */
    public int getInt(){
        return mValue;
    }
    /**
     * 获取显示的性别
     * @return
     */
    public abstract String getValueDisplay();
}