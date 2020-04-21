package com.sdlisilong.mockproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author sdlisilong
 * @date 2020/3/30
 */
public class MockProxyConfig {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String configFilePath = "mockProxy.properties";

    //
    private static String resultFilePath = "classpath*:mockProxy-*.properties";

    //默认后缀
    private static String commonResultFilePrefix = "default";

    //解析的代理接口返回信息
    private Map<String, Map<String, String>> resultAllMap = new HashMap<String, Map<String, String>>();

    //是否开启代理
    private boolean enabled;

    //开启dubbo
    private boolean enabledProxyDubbo;

    //扫描的访问数据库的dao包
    private String daoPackage;

    //需要代理的类前缀
    private List<String> proxyClassPathPrefixList;

    private static MockProxyConfig mockProxyConfig = new MockProxyConfig();

    public static MockProxyConfig getInstance() {
        return mockProxyConfig;
    }

    private MockProxyConfig() {
        loadConfigProperty(this.getClass().getClassLoader());
        loadResultConfigProperty(this.getClass().getClassLoader());
    }

    private void loadConfigProperty(ClassLoader classLoader) {
        try {
            URL url = (classLoader != null ?
                    classLoader.getResource(configFilePath) :
                    ClassLoader.getSystemResource(configFilePath));

            UrlResource resource = new UrlResource(url);
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);

            this.enabled = Boolean.valueOf(properties.getProperty("enabled", Boolean.FALSE.toString()));
            this.enabledProxyDubbo = Boolean.valueOf(properties.getProperty("enabledProxyDubbo", Boolean.TRUE.toString()));
            this.daoPackage = properties.getProperty("daoPackage", "");

            String proxyClassPathPrefix = properties.getProperty("proxyClassPathPrefix", "");

            if (!StringUtils.isEmpty(proxyClassPathPrefix)) {
                List<String> classPathPrefixList = new ArrayList<String>();
                for (String classPathPrefix : StringUtils.commaDelimitedListToStringArray((String) proxyClassPathPrefix)) {
                    classPathPrefixList.add(classPathPrefix);
                }
                this.proxyClassPathPrefixList = classPathPrefixList;
            }

        }
        catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					configFilePath + "]", ex);
		}
    }

    private void loadResultConfigProperty(ClassLoader classLoader) {
        try {
            PathMatchingResourcePatternResolver prpr = new PathMatchingResourcePatternResolver();

            Resource[] resources = prpr.getResources(resultFilePath);

            if (resources != null) {
                for (Resource resource : resources) {
                    EncodedResource encodedResource = new EncodedResource(resource, Charset.forName("UTF-8"));

                    Properties properties = PropertiesLoaderUtils.loadProperties(encodedResource);

                    Map<String, String> map = new HashMap<String, String>();

                    for (Map.Entry<?, ?> entry : properties.entrySet()) {
                        String key = ((String) entry.getKey()).trim();
                        String value = ((String) entry.getValue()).trim();

                        map.put(key, value);
                    }

                    String path = resource.getURL().getPath();
                    String fileName = path.substring(path.lastIndexOf("/") + 1);

                    String prefix = fileName.substring(fileName.indexOf("-") + 1, fileName.lastIndexOf("."));

                    resultAllMap.put(prefix, map);
                }
            } else {
                logger.warn("no mockProxy-*.properties");
            }
        }
        catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					resultFilePath + "]", ex);
		}
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEnabledProxyDubbo() {
        return enabledProxyDubbo;
    }

    public String getDaoPackage() {
        return daoPackage;
    }

    public List<String> getProxyClassPathPrefixList() {
        return proxyClassPathPrefixList;
    }

    public Map<String, String> getResultMap() {
        Map<String, String> map = resultAllMap.get(commonResultFilePrefix);

        return map;
    }

}
