package com.sdlisilong.mockproxy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sdlisilong
 * @date 2020/4/30
 */
public class MockProxyContextUtil {

    private ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>();

    private static MockProxyContextUtil mockProxyContextUtil = new MockProxyContextUtil();

    private MockProxyContextUtil(){};

    public static MockProxyContextUtil getInstance() {
        return mockProxyContextUtil;
    }

    public void setMethodName(String methodName) {
        Map<String, String> map = threadLocal.get();

        if (map == null) {
            map = new HashMap<String, String>();

            threadLocal.set(map);
        }

        map.put("methodName", methodName);
    }

    public String getMethodName() {
        Map<String, String> map = threadLocal.get();

        String methodName = map.get("methodName");

        return methodName;
    }

    public void destroy() {
        threadLocal.remove();
    }
}
