package com.sdlisilong.mockproxy.invocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 提供spi扩展口
 * @author sdlisilong
 * @date 2020/4/2
 */
public class SpiProxyInvocation implements ProxyInvocation {

    private Map<String, List<Object>> proxyConfigMap;

    public SpiProxyInvocation() {
        MockConfigSPILoader.loadMockProxyConfig(this.getClass().getClassLoader());

        this.proxyConfigMap = MockConfigSPILoader.getProxyConfigMap();
    }

    @Override
    public Object preHandle(ParamObject paramObject) throws Exception {

        Method method = paramObject.getMethod();
        String proxyInterfaceName = method.getDeclaringClass().getName();;
        Object[] args = paramObject.getArgs();

        List<Object> objects = proxyConfigMap.get(proxyInterfaceName);

        if (objects == null) {
            return null;
        }

        Object resultObject = null;
        for (Object o : objects) {
            Method proxyMethod = o.getClass().getMethod(method.getName(), method.getParameterTypes());

            if (proxyMethod != null) {
                resultObject = proxyMethod.invoke(o, args);
            }
        }

        if (resultObject != null) {
            return resultObject;
        }

        return null;
    }
}
