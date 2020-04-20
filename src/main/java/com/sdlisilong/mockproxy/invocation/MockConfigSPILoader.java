package com.sdlisilong.mockproxy.invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mock配置spi扩展口
 * @author sdlisilong
 * @date 2020/4/2
 */
public final class MockConfigSPILoader {

	private static final Log logger = LogFactory.getLog(MockConfigSPILoader.class);

	public static final String MOCK_RESOURCE_LOCATION = "META-INF/mock-proxy.config";

	private static final Map<String, List<Object>> proxyConfigMap = new ConcurrentHashMap<String, List<Object>>();

	private MockConfigSPILoader() {
	}

	/**
	 * 初始化
	 * @param classLoader
	 */
	public synchronized static void loadMockProxyConfig(ClassLoader classLoader) {
		if (proxyConfigMap.size() > 0) {
			return;
		}

		String proxyInterface = null;
		try {
			Enumeration<URL> urls = (classLoader != null ?
					classLoader.getResources(MOCK_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(MOCK_RESOURCE_LOCATION));

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					proxyInterface = ((String) entry.getKey()).trim();

					for (String className : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {

						try {
							Class<?> clazz = Class.forName(className.trim());

							Object o = clazz.newInstance();

							List<Object> objects = proxyConfigMap.get(proxyInterface);

							if (objects == null) {
								objects = new ArrayList<Object>();
								proxyConfigMap.put(proxyInterface, objects);
							}

							objects.add(o);

						} catch (ClassNotFoundException e) {
							throw new IllegalArgumentException(
									"Unable to instantiate factory class [" + className.trim() + "] for factory type [" + proxyInterface + "]",
									e);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					MOCK_RESOURCE_LOCATION + "]", ex);
		}
	}

	public static Map<String, List<Object>> getProxyConfigMap() {
		return proxyConfigMap;
	}
}
