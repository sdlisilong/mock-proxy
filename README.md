# mock-proxy
基于spring框架的mock代理组件，可以在Junit里面mock远程调用的接口，数据库返回的值，缓存redis(还没开发)。

摒弃现有的mock框架，需要自己写额外的代理类。

通过本组件，不需要修改业务代码，不需要写mock代理类，只需要根据自己配置添加返回类型的信息，甚至可以零配置，就可以mock接口。本组件针对基本类型有默认返回值，如果是自定义类，会递归查找字段进行赋值。

通过以下配置，只需要在junit里面调用自己本地的接口一样。

**注意：**Map接口实现类如果没有自定义会返回null，List,Set接口实现类，没有自定义会自动构建返回值

# 使用说明(需要在用到的项目里添加信息)
1. 在spring.xml里添加配置
```xml
<bean class="com.sdlisilong.mockproxy.MockProxyBeanDefinitionRegistryPostProcessor"/>
```


2. 在classpath目录下添加mockProxy.properties，内容为

   ```properties
   enabled=true #是否开启代理
   enabledProxyDubbo=true   #开启dubbo代理，默认false
   daoPackage=com.dao       #需要代理数据库返回数据的dao层，添加包名
   ```

3. 添加代理接口返回值配置信息mockProxy-default.properties：

   ```properties
   #返回List<String>的配置
   com.dao.Test.getList=[\"cc\",\"11\"]
   #返回bean信息的配置
   com.dao.Test.getObject={id:1,cashId:22222}
   #返回List<Bean>的配置
   com.dao.Test.getObjectList=[{productId:111,productName:\"测试产品\"}]
   ```

   如上所示，如果需要配置mock接口返回值，需要添加JSON字符串格式。

   你还可以根据返回类型，配置默认返回信息，如：

   如果要配置方法返回值为com.test.entity.TestJavaBean，则可以直接配置

   com.test.entity.TestJavaBean={id:1,name:\\"Hanmeimei\\"}

   如果是基本类型，会有默认返回值String=1,int=1,boolean=true,如果需要自定义，则可以java.lang.String=Test

4. 如果以上都无法满足你，提供了SPI扩展接口，需要在META-INF下面添加文件mock-proxy.config，

   ```
   #被代理的接口，只需要在ProxyClass类里添加要代理的方法，不需要implements接口，只需要实现需要自定义的方法即可，不用都实现。注意：方法要和被代理的方法名字、入参、返回类型都要保持一致
   com.rpc.Service=com.my.ProxyClass
   ```

   # 示例

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:spring/applicationContext.xml"})
public class ServiceTest {

    @Resource
    Service service;
    
    @Resource
    DubboService dubboService;

    @Resource
    Dao dao;

    @Test
    public void testDubbo(){
        com.test.entity.TestJavaBean bean = dubboService.getById("1");
        System.out.println(JSON.toJSONString(bean));
    }

    @Test
    public void testGetProduct(){
        List<String> list = dao.getList();
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testQuery(){
        Set<String> set = dao.getSet("test");
        System.out.println(JSON.toJSONString(set));
    }

    @Test
    public void getList(){
        String id = dao.getId("name");
        System.out.println(JSON.toJSONString(id));
    }
}
```

