package ua.rd.ioc;

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
        BeanBuilder builder = new BeanBuilder (beanDefinition);
        builder.createNewBeanInstance();
        builder.callPostConstructAnnotatedMethod();
        builder.callInitMethod();
        builder.addBenchmarkProxy();

        return  builder.build();
    }

    private BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitions.stream()
                .filter(bd -> Objects.equals(bd.getBeanName(), beanName))
                .findAny().orElseThrow(NoSuchBeanException::new);
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitions.stream()
                .map(BeanDefinition::getBeanName)
                .toArray(String[]::new);
    }

    private class BeanBuilder {
        private BeanDefinition beanDefinition;
        private Object bean;

        public BeanBuilder(BeanDefinition beanDefinition) {
            this.beanDefinition = beanDefinition;
        }

        public void createNewBeanInstance() {
            Class<?> type = beanDefinition.getBeanType();
            if(type == null){
                throw new IllegalArgumentException();
            }
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            if(constructor.getParameterCount() == 0) {
                bean = createBeanWithDefaultConstructor(type);
            } else {
                bean = createBeanWithConstructorWithParams(type);
            }

        }

        public void callPostConstructAnnotatedMethod() {
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

        public void callInitMethod() {
            try {
                Method initMethod = bean.getClass().getMethod("init");
                if(initMethod != null) {
                    initMethod.invoke(bean);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {

            } catch (NoSuchMethodException e) {
            }
        }

        public void addBenchmarkProxy() {
            if(checkBeanMethodsAnnotatedWithBenchmarkProxy()){
                bean = createBenchmarkProxy();
            }
        }

        public Object build() {
            return bean;
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
        private Object createBeanWithConstructorWithParams(Class<?> type) {
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
//            List<Object> parameters = new ArrayList<>();
//            for (Class<?> beanType: parameterTypes) {
//                String parameterName = beanType.getSimpleName();
//                parameterName = Character.toLowerCase(parameterName.charAt(0)) + parameterName.substring(1);
//                parameters.add(getBean(parameterName));
//            }
            Object newBean = null;

            try {
//                newBean = constructor.newInstance(parameters.toArray());
                newBean = constructor.newInstance(Arrays.stream(parameterTypes)
                        .map(e -> Character.toLowerCase(e.getSimpleName().charAt(0)) + e.getSimpleName().substring(1))
                        .map(ApplicationContext.this::getBean)
                        .toArray());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return newBean;
        }
        private Object createBenchmarkProxy() {
            Class<?> beanType = bean.getClass();
            Object newBean = bean;
            return   Proxy.newProxyInstance(beanType.getClassLoader(),
                    beanType.getInterfaces(),
                    (proxy, method, args) -> {
                        Method methodImplemented =  beanType
                                .getMethod(method.getName(), method.getParameterTypes());
                        if(methodImplemented.isAnnotationPresent(Benchmark.class)
                                && checkBenchmarkAnnotationIsActive(methodImplemented)
                                ){
                            Long start = System.currentTimeMillis();
                            Object result = method.invoke(newBean, args);
                            Long stop = System.currentTimeMillis();
                            System.out.println("Duration: " + (stop-start));
                            return result;
                        } else{
                            return method.invoke(newBean, args);
                        }
                    });
        }

        private boolean checkBeanMethodsAnnotatedWithBenchmarkProxy() {
            Method [] methods = bean.getClass().getMethods();

            return Arrays.stream(methods).anyMatch(method -> method.isAnnotationPresent(Benchmark.class)
                    && checkBenchmarkAnnotationIsActive(method));
        }
        private boolean checkBenchmarkAnnotationIsActive(Method method) {
            Benchmark benchmark = method.getAnnotation(Benchmark.class);
            return benchmark.enabled();
        }

    }
}
