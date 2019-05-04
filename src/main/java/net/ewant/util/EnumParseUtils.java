package net.ewant.util;

import java.lang.reflect.Field;

/**
 * Created by admin on 2018/11/9.
 */
public class EnumParseUtils {

    public static <E extends Enum> E get(String v, Class type) {
        if (v == null || v.length() == 0) return null;
        E[] enumConstants = (E[]) type.getEnumConstants();
        for (E t : enumConstants) {
            if(t.toString().equals(v) || t.name().equals(v)){
                return t;
            }
        }
        for (E t : enumConstants) {
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field field : fields) {
                Object value = null;
                try {
                    if (field.isAccessible()) {
                        value  = field.get(t);
                    } else {
                        field.setAccessible(true);
                        value = field.get(t);
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                }
                if(value != null && String.valueOf(value).equals(v)){
                    return t;
                }
            }
        }
        return null;
    }
}
