package ua.rd.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ApplicationContext implements Context {

    private List<BeanDefinition> beanDefinitions;
    private Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(Config config) {
        beanDefinitions = Arrays.asList(config.beanDefinitions());
        init(beanDefinitions);
    }

    private void init(List<BeanDefinition> beanDefinitions) {
        beanDefinitions.forEach(b -> getBean(b.getBeanName()));
    }

    public ApplicationContext() {
        beanDefinitions = Arrays.asList(Config.EMPTY_BEANDEFINITION);//new BeanDefinition[0];
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = getBeanDefinitionByName(beanName);
        Object bean = beans.get(beanName);
        if(bean != null) {
            return bean;
        }
            bean = createNewBean(beanDefinition);
            if (!beanDefinition.isPrototype()) {
                beans.put(beanName, bean);
            }
        return bean;
    }

    private Object createNewBean(BeanDefinition beanDefinition) {
        Object bean = createNewBeanInstance(beanDefinition);
        callPostConstructAnnotatedMethod(bean);
        callInitMethod(bean);
        if(checkBeanMethodsAnnotatedWithBenchmarkProxy(bean)) {
            bean = createBenchmarkProxy(bean);
        }
        return bean;
    }

    private boolean checkBeanMethodsAnnotatedWithBenchmarkProxy(Object bean) {
        Method [] methods = bean.getClass().getMethods();

        return Arrays.stream(methods).anyMatch(method -> method.isAnnotationPresent(Benchmark.class)
                && checkBenchmarkAnnotationIsActive(method));
    }

    private boolean checkBenchmarkAnnotationIsActive(Method method) {
        Benchmark benchmark = method.getAnnotation(Benchmark.class);
        return benchmark.enabled();
    }

    private Object createBenchmarkProxy(Object bean) {
        Class<?> beanType = bean.getClass();
        return Proxy.newProxyInstance(beanType.getClassLoader(),
                beanType.getInterfaces(),
                (proxy, method, args) -> {
                    Method methodImplemented =  bean.getClass()
                            .getMethod(method.getName(), method.getParameterTypes());
                    if(methodImplemented.isAnnotationPresent(Benchmark.class)
                            && checkBenchmarkAnnotationIsActive(methodImplemented)
                            ){
                        Long start = System.currentTimeMillis();
                        Object result = method.invoke(bean, args);
                        Long stop = System.currentTimeMillis();
                        System.out.println("Duration: " + (stop-start));
                        return result;
                    } else{
                        return method.invoke(bean, args);
                    }
                });
    }
//    private Object createBenchmarkProxy(Object bean) {
//        Class<?> beanType = bean.getClass();
//        MethodInterceptor handler = new MethodInterceptor() {
//            @Override
//            public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
//                if(method.isAnnotationPresent(Benchmark.class)){
//                        Long start = System.currentTimeMillis();
//                        Object result = method.invoke(bean, args);
//                        Long stop = System.currentTimeMillis();
//                        System.out.println("Duration: " + (stop-start));
//                        return result;
//                    } else{
//                        return method.invoke(bean, args);
//                    }
//            }
//        };
//        return Enhancer.create(beanType, handler);
//    }

    private void callPostConstructAnnotatedMethod(Object bean) {
        Method [] methods = bean.getClass().getMethods();
        Arrays.stream(methods).filter(m -> m.isAnnotationPresent(MyPostConstruct.class))
                .forEach(m -> {
                    try {
                        m.invoke(bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void callInitMethod(Object bean) {
        try {
            Method initMethod = bean.getClass().getMethod("init");
            if(initMethod != null) {
                initMethod.invoke(bean);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {

        } catch (NoSuchMethodException e) {
        }
    }

    private BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitions.stream()
                .filter(bd -> Objects.equals(bd.getBeanName(), beanName))
                .findAny().orElseThrow(NoSuchBeanException::new);
    }

    private Object createNewBeanInstance(BeanDefinition bd) {
        Class<?> type = bd.getBeanType();
        if(type == null){
            throw new IllegalArgumentException();
        }
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        Object newBean = null;
        if(constructor.getParameterCount() == 0) {
            newBean = createBeanWithDefaultConstructor(type);
        } else {
            newBean = createBeanWithConstructorWithParams(type);
        }
        return newBean;
    }

    private Object createBeanWithConstructorWithParams(Class<?> type) {
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        List<Object> parameters = new ArrayList<>();
        for (Class<?> beanType: parameterTypes) {
            String parameterName = beanType.getSimpleName();
            parameterName = Character.toLowerCase(parameterName.charAt(0)) + parameterName.substring(1);
            parameters.add(getBean(parameterName));
        }
        Object newBean = null;
        try {
            newBean = constructor.newInstance(parameters.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newBean;
    }

    private Object createBeanWithDefaultConstructor(Class<?> type) {
        Object newBean;
        try {
           newBean = type.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return newBean;
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitions.stream()
                .map(BeanDefinition::getBeanName)
                .toArray(String[]::new);
    }
}
