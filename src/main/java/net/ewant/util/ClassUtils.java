package net.ewant.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by admin on 2018/12/14.
 */
public class ClassUtils {
    public static Class<?> getActualType(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[index];
            if(type instanceof Class){
                return (Class<?>)type;
            }else{
                return getActualType(type.getClass(), index);
            }
        }else if(!Object.class.equals(clazz)){
            return getActualType((Class<?>)genericSuperclass, index);
        }
        return clazz;
    }
}
