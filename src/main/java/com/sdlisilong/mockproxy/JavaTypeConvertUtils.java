package com.sdlisilong.mockproxy;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author sdlisilong
 * @date 2020/4/15
 */
public class JavaTypeConvertUtils {

//    (Boolean.class, boolean.class);
//    (Byte.class, byte.class);
//    (Character.class, char.class);
//    (Double.class, double.class);
//    (Float.class, float.class);
//    (Integer.class, int.class);
//    (Long.class, long.class);
//    (Short.class, short.class);

    public static void setStringValue(Map<String, String> resultMap, Object returnObject, Field field) throws IllegalAccessException {
        field.set(returnObject, getStringValue(resultMap));
    }

    public static String getStringValue(Map<String, String> resultMap) {
        String value = resultMap.get(String.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return value;
    }

    /**
     * 给基本类型设置值
     */
    public static void setPrimitiveValue(Map<String, String> resultMap, Object returnObject, Field field) throws IllegalAccessException {
        if (field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class)) {
            field.set(returnObject, getBoolean(resultMap));
        } else if (field.getType().isAssignableFrom(Byte.class) || field.getType().isAssignableFrom(byte.class)) {
            field.set(returnObject, getByte(resultMap));
        } else if (field.getType().isAssignableFrom(Character.class) || field.getType().isAssignableFrom(char.class)) {
            field.set(returnObject, getCharacter(resultMap));
        } else if (field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(double.class)) {
            field.set(returnObject, getDouble(resultMap));
        } else if (field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(float.class)) {
            field.set(returnObject, getFloat(resultMap));
        } else if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class)) {
            field.set(returnObject, getInteger(resultMap));
        } else if (field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(long.class)) {
            field.set(returnObject, getLong(resultMap));
        } else if (field.getType().isAssignableFrom(Short.class) || field.getType().isAssignableFrom(short.class)) {
            field.set(returnObject, getShort(resultMap));
        }
    }

    /**
     * 获取值
     */
    public static Object getPrimitiveValue(Map<String, String> resultMap, Class<?> clazz) {
        if (clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)) {
            return getBoolean(resultMap);
        } else if (clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class)) {
            return getByte(resultMap);
        } else if (clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(char.class)) {
            return getCharacter(resultMap);
        } else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return getDouble(resultMap);
        } else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return getFloat(resultMap);
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) {
            return getInteger(resultMap);
        } else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return getLong(resultMap);
        } else if (clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) {
            return getShort(resultMap);
        }
        return null;
    }

    public static Boolean getBoolean(Map<String, String> resultMap) {
        String value = resultMap.get(Boolean.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(boolean.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = Boolean.TRUE.toString();
        }

        return Boolean.valueOf(value);
    }

    public static Byte getByte(Map<String, String> resultMap) {
        String value = resultMap.get(Byte.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(byte.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return Byte.valueOf(value);
    }

    public static Character getCharacter(Map<String, String> resultMap) {
        String value = resultMap.get(Character.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(char.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return value.charAt(0);
    }

    public static Double getDouble(Map<String, String> resultMap) {
        String value = resultMap.get(Double.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(double.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1.00";
        }

        return Double.valueOf(value);
    }

    public static Float getFloat(Map<String, String> resultMap) {
        String value = resultMap.get(Float.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(float.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1.00";
        }

        return Float.valueOf(value);
    }

    public static Integer getInteger(Map<String, String> resultMap) {
        String value = resultMap.get(Integer.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(int.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return Integer.valueOf(value);
    }

    public static Long getLong(Map<String, String> resultMap) {
        String value = resultMap.get(Long.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(long.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return Long.valueOf(value);
    }

    public static Short getShort(Map<String, String> resultMap) {
        String value = resultMap.get(Short.class.getName());

        if (StringUtils.isEmpty(value)) {
            value = resultMap.get(short.class.getName());
        }

        if (StringUtils.isEmpty(value)) {
            value = "1";
        }

        return Short.valueOf(value);
    }
}
