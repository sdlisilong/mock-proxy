package com.sdlisilong.mockproxy;

import com.google.gson.Gson;
import com.sdlisilong.mockproxy.invocation.DefaultResultInvocation;
import com.sdlisilong.mockproxy.invocation.ParamObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * @author sdlisilong
 * @date 2020/3/28
 */
public class MockProxyCglibFactory {

    Logger logger = LoggerFactory.getLogger(MockProxyCglibFactory.class);

    public static Object getProxy(Class clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        // 回调方法
        Callback noOp= NoOp.INSTANCE;   //不代理
        enhancer.setCallbacks(new Callback[]{new MockProxyMethodInterceptor(), noOp});
        enhancer.setCallbackFilter(new CallbackFilterImpl());
        // 创建代理对象
        return enhancer.create();
    }

    private static class CallbackFilterImpl implements CallbackFilter {
        @Override
        public int accept(Method method) {
            if (ReflectionUtils.isToStringMethod(method) || ReflectionUtils.isEqualsMethod(method) || ReflectionUtils.isHashCodeMethod(method))
                return 1;
            else
                return 0;
        }

    }

    static class MockProxyMethodInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

            String format = String.format("MockProxyCglibHandler代理类请求;method:%s; args:%s;",
                    method.getDeclaringClass()+"; methodName:"+method.getName(), Arrays.toString(args));

            System.out.println(format);

//            Object o1 = methodProxy.invokeSuper(object, args);

            DefaultResultInvocation defaultResultInvocation = DefaultResultInvocation.getInstance();

            ParamObject paramObject = new ParamObject();
            paramObject.setMethod(method);
            Object result = defaultResultInvocation.invoke(paramObject);

            System.out.println("MockProxyCglibHandler代理类返回：" + new Gson().toJson(result));

            return result;
        }

    }


    public static void main(String[] args) {
//        ProxyTest proxy = (ProxyTest) MockProxyCglibFactory.getProxy(ProxyTest.class);
//
//        proxy.print("-----test");
//
//        System.out.println(proxy.toString());


        String s = "[\"aaa\"]";

        Set set = new Gson().fromJson(s, Set.class);
        System.out.println(set.size());
    }
}
