package com.sdlisilong.mockproxy.invocation;

import com.google.gson.Gson;
import com.sdlisilong.mockproxy.MockProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 通过配置文件获取返回值
 * @author sdlisilong
 * @date 2020/4/2
 */
public class MockProxyConfigProxyInvocation implements ProxyInvocation {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    MockProxyConfig mockProxyConfig = MockProxyConfig.getInstance();

    public MockProxyConfigProxyInvocation() {
    }

    @Override
    public Object preHandle(ParamObject paramObject) throws Exception {

        Method method = paramObject.getMethod();
        String proxyInterfaceName = method.getDeclaringClass().getName();;
        Object[] args = paramObject.getArgs();


        String key = proxyInterfaceName + "." + method.getName();

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
