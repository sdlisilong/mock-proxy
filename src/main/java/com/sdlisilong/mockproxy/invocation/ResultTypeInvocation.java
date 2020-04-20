package com.sdlisilong.mockproxy.invocation;

import com.google.gson.Gson;
import com.sdlisilong.mockproxy.JavaTypeConvertUtils;
import com.sdlisilong.mockproxy.MockProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通过查找返回结果类型
 * @author sdlisilong
 * @date 2020/4/2
 */
public class ResultTypeInvocation implements ProxyInvocation {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    MockProxyConfig mockProxyConfig = MockProxyConfig.getInstance();

    public ResultTypeInvocation() {
    }

    @Override
    public Object preHandle(ParamObject paramObject) throws Exception {

        Method method = paramObject.getMethod();
        String proxyInterfaceName = method.getDeclaringClass().getName();;
        Object[] args = paramObject.getArgs();

        Class<?> returnClass = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();

        Map<String, String> resultMap = mockProxyConfig.getResultMap();

        if (genericReturnType instanceof ParameterizedType) {
            return getFieldValue(returnClass, (ParameterizedType)genericReturnType, resultMap);
        } else {
            return getFieldValue(returnClass, null, resultMap);
        }
    }

    /**
     * 递归获取字段属性
     * @param returnClass
     * @return
     */
    private Object getFieldValue(Class<?> returnClass, ParameterizedType genericReturnType, Map<String, String> resultMap) {

        try {
            //先判断是否String
            if (returnClass.isAssignableFrom(String.class)) {
                return JavaTypeConvertUtils.getStringValue(resultMap);
            }

            //是否为基本类型
            if (ClassUtils.isPrimitiveOrWrapper(returnClass)) {
                return JavaTypeConvertUtils.getPrimitiveValue(resultMap, returnClass);
            }

            //是否为数组
            if (returnClass.isArray()) {
                Class<?> componentType = returnClass.getComponentType();
                String value = resultMap.get(returnClass.getName());
                if (StringUtils.isEmpty(value)) {
                    return null;
                } else {
                    Object o = new Gson().fromJson(value, componentType);
                    return o;
                }
            }
            //list
            if (List.class.isAssignableFrom(returnClass)) {
                if (genericReturnType == null) {
                    Object genericObject = new Object();
                    if (returnClass.isInterface()) {
                        List<Object> list = new ArrayList<Object>();

                        list.add(genericObject);

                        return list;
                    } else {
                        Object returnObject = returnClass.newInstance();

                        Method method = returnClass.getMethod("add", Object.class);

                        method.invoke(returnObject, genericObject);  //调用array的add方法传入的值是java

                        return returnObject;
                    }

                } else {
                    Object o = getFieldValue((Class) genericReturnType.getActualTypeArguments()[0], null, resultMap);

                    if (returnClass.isInterface()) {
                        List<Object> list = new ArrayList<Object>();

                        list.add(o);

                        return list;
                    } else {
                        Object returnObject = returnClass.newInstance();

                        Method method = returnClass.getMethod("add", Object.class);

                        method.invoke(returnObject, o);  //调用array的add方法传入的值是java

                        return returnObject;
                    }
                }
            }
            //set
            if (Set.class.isAssignableFrom(returnClass)) {
                if (genericReturnType == null) {
                    Object genericObject = new Object();
                    if (returnClass.isInterface()) {
                        Set<Object> set = new HashSet<Object>();

                        set.add(genericObject);

                        return set;
                    } else {
                        Object returnObject = returnClass.newInstance();

                        Method method = returnClass.getMethod("add", Object.class);

                        method.invoke(returnObject, genericObject);  //调用array的add方法传入的值是java

                        return returnObject;
                    }

                } else {
                    Object o = getFieldValue((Class) genericReturnType.getActualTypeArguments()[0], null, resultMap);

                    if (returnClass.isInterface()) {
                        Set<Object> set = new HashSet<Object>();

                        set.add(o);

                        return set;
                    } else {
                        Object returnObject = returnClass.newInstance();

                        Method method = returnClass.getMethod("add", Object.class);

                        method.invoke(returnObject, o);

                        return returnObject;
                    }
                }
            }

            //map
            if (Map.class.isAssignableFrom(returnClass)) {
                String value = resultMap.get(returnClass.getName());
                if (StringUtils.isEmpty(value)) {
                    return null;
                } else {
                    Object o = new Gson().fromJson(value, returnClass);
                    return o;
                }
            }

            //如果为接口
            if (returnClass.isInterface()) {
                String value = resultMap.get(returnClass.getName());
                if (StringUtils.isEmpty(value)) {
                    return null;
                } else {
                    Object o = new Gson().fromJson(value, returnClass);
                    return o;
                }
            }

            //如果为java类，则返回null，后续扩展
            if (isJavaClass(returnClass)) {
                return null;
            }

            //自定义类
            Object returnObject = returnClass.newInstance();

            Field[] fields = returnClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Type genericType = field.getGenericType();

                if (genericType instanceof ParameterizedType) {
                    field.set(returnObject, getFieldValue(field.getType(), (ParameterizedType)genericType, resultMap));
                } else {
                    field.set(returnObject, getFieldValue(field.getType(), null, resultMap));
                }
            }

            return returnObject;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断一个类是JAVA类型还是用户定义类型
     * @param clz
     * @return
     */
    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    public static void main(String[] args) throws NoSuchMethodException {
//        String a = "a";
//        int b = 1;
//        Integer c = 2;
//        int[] d = {1};
//        Integer[] e = {2};
//
//        System.out.println(ClassUtils.isPrimitiveWrapper(c.getClass()));
//        System.out.println(ClassUtils.isPrimitiveWrapper(d.getClass()));
//        System.out.println(d.getClass().isArray());
//        System.out.println(e.getClass().getComponentType().getName());
//        System.out.println(boolean.class.getName());
//
//        try {
//            Test returnObject = new Test();
//            Map<String, String> resultMap = new HashMap();
//
//            for (Field field : Test.class.getFields()) {
//                System.out.println(field.getType());
//
//                JavaTypeConvertUtils.setPrimitiveValue(resultMap, returnObject, field);
//            }
//
//            System.out.println(new Gson().toJson(returnObject));
//
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }

        List<Test> list = new ArrayList();

        System.out.println(list.getClass());

        Method method = Test.class.getMethod("getList", null);

        System.out.println(method.getReturnType());
        System.out.println(method.getGenericReturnType());

    }

    public static class Test {
        public boolean a;
        public Boolean b;
        public Byte c;
        public byte d;
        public Character e;
        public char f;
        public Double g;
        public double h;
        public Float i;
        public float j;
        public Integer k;
        public int l;
        public Long m;
        public long n;
        public Short o;
        public short p;

        public List<String> getList() {
            System.out.println("....");
            return null;
        }
    }


}
