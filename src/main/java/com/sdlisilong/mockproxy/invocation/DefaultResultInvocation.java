package com.sdlisilong.mockproxy.invocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sdlisilong
 * @date 2020/4/2
 */
public class DefaultResultInvocation {

    private static List<ProxyInvocation> invocationList = new ArrayList<ProxyInvocation>();

    private static DefaultResultInvocation defaultResultInvocation = new DefaultResultInvocation();

    public static DefaultResultInvocation getInstance() {
        return defaultResultInvocation;
    }

    private DefaultResultInvocation() {
        initInterceptor();
    }

    private void initInterceptor() {
        invocationList.add(new SpiProxyInvocation());
        invocationList.add(new MockProxyConfigProxyInvocation());
        invocationList.add(new ResultTypeInvocation());
    }

    public Object invoke(ParamObject paramObject) throws Exception {
        Object result = null;

        for (ProxyInvocation invocation : invocationList) {
            result = invocation.preHandle(paramObject);
            if (result != null) {
                break;
            }
        }

        return result;
    }
}
