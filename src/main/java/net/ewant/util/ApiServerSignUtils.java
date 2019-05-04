package net.ewant.util;

/**
 * Created by admin on 2018/12/22.
 */
public class ApiServerSignUtils {
    private static final int TIME_DIFF = 15000;
    private static final String KEY = "apiservice123456";
    public static String sign(){
        Long timestamp = System.currentTimeMillis();
        return CipherUtils.AesHex.encrypt(String.valueOf(timestamp), KEY);
    }

    public static boolean verify(String sign){
        try {
            String decrypt = CipherUtils.AesHex.decrypt(sign, KEY);
            long timestamp = Long.parseLong(decrypt);
            return System.currentTimeMillis() - timestamp < TIME_DIFF;
        } catch (NumberFormatException e) {
        }
        return false;
    }

}
