package net.ewant.util;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.regex.Pattern;

/**
 * Created by admin on 2018/11/9.
 */
public class MathUtils {

    private static final Pattern REGEX_NUMBER = Pattern.compile("^[0-9\\-]?[0-9]+(\\.[0-9]+)*$");

    private static final Pattern REGEX_FLOAT = Pattern.compile("^[0-9\\-]?[0-9]+\\.[0-9]+$");

    public static boolean isNumber(String key){
        return REGEX_NUMBER.matcher(key).matches();
    }

    public static boolean isFloat(String key){
        return REGEX_FLOAT.matcher(key).matches();
    }

    /**
     * 对给定数值保留固定小数位
     * @param num 浮点数值
     * @param fractionDigits 保留几位小数
     * @param roundUp 是否四舍五入
     * @return
     */
    public static double toFixed(double num, int fractionDigits, boolean roundUp) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        // 保留小数
        nf.setMaximumFractionDigits(fractionDigits);
        if(roundUp){
            nf.setRoundingMode(RoundingMode.HALF_UP);
        }
        return Double.parseDouble(nf.format(num).replaceAll(",",""));
    }

    /**
     * 根据给定数值与手续费配置方式，计算手续费
     * @param num 浮点数值
     * @param fee 配置的手续费（有可能包含百分百）
     * @return
     */
    public static double parseFee(double num, String fee) {
        if(fee == null || fee.length() == 0){
            return 0;
        }
        if(!fee.contains("%")){
            // TODO 手续费单位是BTC，需要转成μBTC
            return isNumber(fee) ? Double.parseDouble(fee) * 1000000 : 0;
        }
        String newFee = fee.substring(0, fee.length() - 1);
        if(isNumber(newFee)){
            return num * (Double.parseDouble(newFee) / 100);
        }
        return 0;
    }

    public static void main(String[] args) {
        String key = "-123.0";
        System.out.println(isNumber(key));
        System.out.println(isFloat(key));

        System.out.println(toFixed(0.125D, 2, false));
    }
}
