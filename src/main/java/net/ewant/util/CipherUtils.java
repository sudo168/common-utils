package net.ewant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CipherUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CipherUtils.class);

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//默认的加密算法
    private static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_CHARTSET = "utf-8";//默认字符编码
    private static final String DEFAULT_KEY = "ewant12345678910";//默认字符编码

    public static class AesHex{
        public static String encrypt(String content, String key) {
            try {
                return CipherUtils.encrypt(content, key, true);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
        public static String decrypt(String content, String key) {
            try {
                return CipherUtils.decrypt(content, key, true);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
    }

    public static class AesBase64{
        public static String encrypt(String content, String key) {
            try {
                return CipherUtils.encrypt(content, key, false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
        public static String decrypt(String content, String key) {
            try {
                return CipherUtils.decrypt(content, key, false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
    }

    private static String[] generaKeys(String key) throws Exception{
        if(key == null){
            return new String[]{DEFAULT_KEY};
        }
        int keyLength = 16;
        int diff = keyLength - key.length();
        if(diff == 0){
            return new String[]{key};
        }else if(diff < 0){
            int j = diff + keyLength;
            if(j == 0){
                return new String[]{key.substring(0, keyLength), key.substring(keyLength)};
            }else if(j < 0){
                return new String[]{key.substring(0, keyLength), key.substring(keyLength, keyLength << 1)};
            }else{
                String ivStr = key.substring(keyLength);
                for (int i = 0; i < j; i++) {
                    ivStr += "0";
                }
                return new String[]{key.substring(0, keyLength), ivStr};
            }
        }else{
            for (int i = 0; i < diff; i++) {
                key += "0";
            }
            return new String[]{key};
        }
    }

    private static byte[] resultDecode(String content, boolean hex){
        if(hex){
            if (content.isEmpty())
                throw new IllegalArgumentException("this content must not be empty");

            String hexString = content.toLowerCase();
            final byte[] byteArray = new byte[hexString.length() / 2];
            int k = 0;
            for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
                byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
                byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
                byteArray[i] = (byte) (high << 4 | low);
                k += 2;
            }
            return byteArray;
        }else{
            return Base64.getDecoder().decode(content);
        }
    }

    private static String resultEncode(byte[] content, boolean hex){
        if(hex){
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < content.length; i++) {
                int v = content[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }
        return new String(Base64.getEncoder().encode(content));
    }

    /**
     * AES 加密操作
     *
     * @param content 待加密内容
     * @param key 加密密码
     * @return 返回Base64转码后的加密数据
     */
    private static String encrypt(String content, String key, boolean hex) throws Exception {

        String[] generaKeys = generaKeys(key);
        if(generaKeys.length > 1){
            return encryptIV(content, generaKeys[0], generaKeys[1], hex);
        }

        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器

        byte[] byteContent = content.getBytes(DEFAULT_CHARTSET);

        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(generaKeys[0]));// 初始化为加密模式的密码器

        byte[] result = cipher.doFinal(byteContent);// 加密

        return resultEncode(result, hex);//通过Base64转码返回
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param key
     * @return
     */
    private static String decrypt(String content, String key, boolean hex) throws Exception {
        String[] generaKeys = generaKeys(key);
        if(generaKeys.length > 1){
            return decryptIV(content, generaKeys[0], generaKeys[1], hex);
        }
        //实例化
        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

        //使用密钥初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, generateSecretKey(generaKeys[0]));

        //执行操作
        byte[] result = cipher.doFinal(resultDecode(content, hex));

        return new String(result, DEFAULT_CHARTSET);
    }

    /**
     * 生成加密秘钥
     *
     * @return
     */
    private static SecretKeySpec generateSecretKey(String key) throws Exception {

        byte[] encode = key.getBytes(DEFAULT_CHARTSET);

        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);

        //AES 要求密钥长度为 128，192，256
        kg.init(128, new SecureRandom(encode));

        //生成一个密钥
        SecretKey secretKey = kg.generateKey();

        return new SecretKeySpec(encode, KEY_ALGORITHM);// 转换为AES专用密钥
        //return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// 转换为AES专用密钥（含随机数）
    }

    private static String encryptIV(String content, String key, String iv, boolean hex) throws Exception {

        Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);// 创建密码器

        byte[] byteContent = content.getBytes(DEFAULT_CHARTSET);

        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(key), new IvParameterSpec(iv.getBytes(DEFAULT_CHARTSET)));// 初始化为加密模式的密码器

        byte[] result = cipher.doFinal(byteContent);// 加密

        return resultEncode(result, hex);//通过Base64转码返回
    }

    private static String decryptIV(String content, String key, String iv, boolean hex) throws Exception {

        //实例化
        Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);

        //使用密钥初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, generateSecretKey(key), new IvParameterSpec(iv.getBytes(DEFAULT_CHARTSET)));

        //执行操作
        byte[] result = cipher.doFinal(resultDecode(content, hex));

        return new String(result, DEFAULT_CHARTSET);
    }

    public static class MD5{
        public static String encrypt(String content){
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] digest = md5.digest(content.getBytes(DEFAULT_CHARTSET));
                return resultEncode(digest, true);
            } catch (Exception e) {
            }
            return null;
        }
        public static String encryptBase64(String content){
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] digest = md5.digest(content.getBytes(DEFAULT_CHARTSET));
                return resultEncode(digest, false);
            } catch (Exception e) {
            }
            return null;
        }
    }

    public static void main(String[] args) throws Exception {

        /*String s = "您好";

        String key = "12345678901234567";

        System.out.println("明文:" + s);

        String s1 = CipherUtils.AesHex.encrypt(s, key);

        System.out.println("加密1:" + s1);

        System.out.println("解密1:" + CipherUtils.AesHex.decrypt(s1, key));

        System.out.println("解密2:" + CipherUtils.AesHex.decrypt("1bf48deac60e2bac61aa0bf3b39d8a68", null));

        InetAddress[] ips=InetAddress.getAllByName("www.baidu.com");
        for(InetAddress ip: ips){
            System.out.println(ip.toString());
            System.out.println("Address: "+ip.getHostAddress());
            System.out.println("Name: "+ip.getHostName());
        }

        System.out.println("MD5 Hex: "+ CipherUtils.MD5.encrypt(s));
        System.out.println("MD5 Base64: "+ CipherUtils.MD5.encryptBase64(s));*/

        System.out.println("加密 Hex: " + CipherUtils.AesHex.encrypt("50238", "20190222114825065750956049147763"));
        System.out.println("解密 Hex: " + CipherUtils.AesHex.decrypt("0d91768ff2e53dbdb45bcdd025720d14", "20190222114825065750956049147763"));
    }

}
