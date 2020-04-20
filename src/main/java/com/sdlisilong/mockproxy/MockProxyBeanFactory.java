package com.sdlisilong.mockproxy;

import org.springframework.beans.factory.FactoryBean;


/**
 * @author sdlisilong
 * @date 2020/3/27
 */
public class MockProxyBeanFactory<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

    public MockProxyBeanFactory(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public T getObject() throws Exception {
        return (T) MockProxyCglibFactory.getProxy(interfaceType);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
