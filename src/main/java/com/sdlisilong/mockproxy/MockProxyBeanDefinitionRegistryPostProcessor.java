package com.sdlisilong.mockproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author sdlisilong
 * @date 2020/3/27
 */
public class MockProxyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, ResourceLoaderAware {

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        //加载配置文件
        MockProxyConfig mockProxyConfig = MockProxyConfig.getInstance();
        //如果没开启，直接返回
        if (!mockProxyConfig.isEnabled()) {
            return;
        }

        //代理dao
        if (!StringUtils.isEmpty(mockProxyConfig.getDaoPackage())) {
            proxyBaseDao(registry, mockProxyConfig);
        }

        //代理dubbo
        if (mockProxyConfig.isEnabledProxyDubbo()) {
            proxyDubbo(registry, mockProxyConfig);
        }

        //类路径前缀代理
        proxyClassPathPrefix(registry, mockProxyConfig);

//        System.out.println("-------start-------------------------------------------------");
//        for (String name : registry.getBeanDefinitionNames()) {
//            BeanDefinition beanDefinition = registry.getBeanDefinition(name);
//            System.out.println(beanDefinition.getBeanClassName());
//        }
//        System.out.println("-------end-------------------------------------------------");

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    /**
     * 代理dao
     * @param registry
     * @param mockProxyConfig
     */
    private void proxyBaseDao(BeanDefinitionRegistry registry, MockProxyConfig mockProxyConfig) {
        Set<Class<?>> proxyClassSet = this.scannerBasePackages(mockProxyConfig.getDaoPackage());

        for (Class beanClazz : proxyClassSet) {

            //内部类
            if (beanClazz.getEnclosingClass() != null) {
                continue;
            }

            String decapitalize = Introspector.decapitalize(beanClazz.getSimpleName());

            if (!StringUtils.hasLength(decapitalize)) {
                continue;
            }

            registBean(registry, beanClazz, decapitalize);
        }
    }

    /**
     * 代理dubbo
     * @param registry
     * @param mockProxyConfig
     */
    private void proxyDubbo(BeanDefinitionRegistry registry, MockProxyConfig mockProxyConfig) {

        String[] names = registry.getBeanDefinitionNames();
        try {
            for (String beanName : names) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);

                if (beanDefinition instanceof RootBeanDefinition) {
                    String beanClassName = ((RootBeanDefinition) beanDefinition).getBeanClassName();
                    if ("com.alibaba.dubbo.config.spring.ReferenceBean".equals(beanClassName)) {
                        PropertyValue interfaceProperty = beanDefinition.getPropertyValues().getPropertyValue("interface");

                        String interfaceName = String.valueOf(interfaceProperty.getValue());

                        registBean(registry, Class.forName(interfaceName), beanName);
                    } else if (beanClassName.startsWith("com.alibaba.dubbo.config")) {
                        //去掉配置信息
                        if (beanName.startsWith("${") && beanName.endsWith("}")) {
                            continue;
                        }
                        try {
                            registry.removeBeanDefinition(beanName);
                        } catch (NoSuchBeanDefinitionException e) {
                            logger.warn("beanName:{}, No Such BeanDefinition,continue");
                        }
                    }
                }


            }
        } catch (ClassNotFoundException e) {
            logger.error("classNotFoundException: {}", e);
        }
    }
    /**
     * 代理类路径前缀
     * @param registry
     * @param mockProxyConfig
     */
    private void proxyClassPathPrefix(BeanDefinitionRegistry registry, MockProxyConfig mockProxyConfig) {

        List<String> proxyClassPathPrefixList = mockProxyConfig.getProxyClassPathPrefixList();

        if (proxyClassPathPrefixList == null || proxyClassPathPrefixList.size() == 0) {
            return;
        }

        String[] names = registry.getBeanDefinitionNames();
        try {
            for (String beanName : names) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);

                String beanClassName = beanDefinition.getBeanClassName();

                for (String proxyClassPathPrefix : proxyClassPathPrefixList) {
                    if (beanClassName.startsWith(proxyClassPathPrefix)) {
                        registBean(registry, Class.forName(beanClassName), beanName);
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error("classNotFoundException: {}", e);
        }
    }

    private void registBean(BeanDefinitionRegistry registry, Class beanClazz, String beanName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
        GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();

        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClazz);

        definition.setBeanClass(MockProxyBeanFactory.class);

        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        registry.registerBeanDefinition(beanName, definition);
    }

    /**
     * 根据包路径获取包及子包下的所有类
     * @param basePackage basePackage
     * @return Set<Class<?>> Set<Class<?>>
     */
    private Set<Class<?>> scannerBasePackages(String basePackage) {
        Set<Class<?>> set = new LinkedHashSet<Class<?>>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                        set.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return set;
    }

    private MetadataReaderFactory metadataReaderFactory;

    private ResourcePatternResolver resourcePatternResolver;

    private ApplicationContext applicationContext;

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.getEnvironment().resolveRequiredPlaceholders(basePackage));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }
}
