package com.milanac007.demo.im.net;

import com.milanac007.demo.im.utils.Preferences;

public class NetConstants {
    public static final String customerID = "IM"; //用于版本更新
    public static String HostName;
    public static String FILE_SERVER_URL;  //文件服务器
    public static String URL_UPLOADFILE;

    public static void init() {
        HostName = Preferences.getHostName();
        FILE_SERVER_URL = String.format("http://%s:8080/", HostName);
        URL_UPLOADFILE = FILE_SERVER_URL + "uploadfile";
    }

    static {
        init();
    }

    public static String getBaseURL() {
        return BaseURL;
    }

    public static final String BaseURL =  "https://" + HostName +":18444/srhypt";

    //登录
    public static final String LoginURL = BaseURL + "/users/login";

    //登出
    public static final String logoutURL = BaseURL + "/users/logout";

    //消息server
    public static final String MsgServerURL = BaseURL + "/msgserver";



//    public static void XXX(){
//        try {
//            String[] keyInfo = LogEncryptKeyCfg.getKeyInfo(keyType);
//            BigInteger b1 = new BigInteger(keyInfo[1], 16);
//            BigInteger b2 = new BigInteger("00010001", 16);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
//            PublicKey publicKey = keyFactory.generatePublic(keySpec);
//            mkey = new byte[32];
//            SecureRandom sr = new SecureRandom();
//            sr.nextBytes(mkey);
//            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            cipher.update(mkey);
//            encKey = cipher.doFinal();
//
//            instance = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
//            SecretKeySpec securekey = new SecretKeySpec(mkey, "AES");
//            instance.init(Cipher.ENCRYPT_MODE, securekey);
////                byte[] update = instance.update(encKey);
//
//            os.write(Hex.decode(keyInfo[0]));
//            byte[] abExName = new byte[4];
//            byte[] bytes = type.getBytes();
//            if (bytes != null) {
//                System.arraycopy(bytes, 0, abExName, 0, bytes.length <= 4 ? bytes.length : 4);
//            }
//            os.write(abExName);
//            os.write(encKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
