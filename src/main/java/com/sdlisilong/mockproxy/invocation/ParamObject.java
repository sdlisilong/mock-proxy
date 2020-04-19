package java.com.sdlisilong.mockproxy.invocation;

import java.lang.reflect.Method;

/**
 * @author sdlisilong
 * @date 2020/4/14
 */
public class ParamObject {

    private Method method;

    private Object[] args;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
