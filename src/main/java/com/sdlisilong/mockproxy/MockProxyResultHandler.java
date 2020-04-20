package com.sdlisilong.mockproxy;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author sdlisilong
 * @date 2020/3/30
 */
public class MockProxyResultHandler {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    static MockProxyConfig mockProxyConfig = MockProxyConfig.getInstance();

    public static Object getResult(Method method) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        String key = className + "." + methodName;

        Map<String, String> map = mockProxyConfig.getResultMap();

        String value = map.get(key);

        System.out.println(method.getReturnType());

        Type returnType = method.getGenericReturnType();// 返回类型

//        if (returnType instanceof ParameterizedType)/**//* 如果是泛型类型 */{
//            Type[] types = ((ParameterizedType) returnType)
//                    .getActualTypeArguments();// 泛型类型列表
//            System.out.println("  TypeArgument: ");
//            for (Type type : types) {
//                System.out.println("   " + type);
//            }
//        }

        if (!StringUtils.isEmpty(value)) {
            Object o = new Gson().fromJson(value, returnType);
            return o;
        }

        return null;
    }
}
