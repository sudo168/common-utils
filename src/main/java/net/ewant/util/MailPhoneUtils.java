package net.ewant.util;

import java.util.regex.Pattern;

/**
 * Created by huangzh on 2018/11/2.
 */
public class MailPhoneUtils {
    /**
     * 正则表达式：验证手机号
     */
    private static final Pattern REGEX_MOBILE = Pattern.compile("^((13[0-9])|(14[5-9])|(15[^4,\\D])|(16[256])|(17[0-8])|(18[0-9])|(19[1389]))\\d{8}$");

    /**
     * 正则表达式：验证邮箱
     */
    private static final Pattern REGEX_EMAIL = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

    /**
     * 校验手机号
     *
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobile(String mobile) {
        return REGEX_MOBILE.matcher(mobile).matches();
    }

    /**
     * 校验邮箱
     *
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail(String email) {
        return REGEX_EMAIL.matcher(email).matches();
    }

}
