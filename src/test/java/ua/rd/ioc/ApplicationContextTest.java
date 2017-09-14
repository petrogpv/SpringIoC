package ua.rd.ioc;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ApplicationContextTest {

    @Test(expected = NoSuchBeanException.class)
    public void getBeanWithEmptyContext() throws Exception {
        Context context = new ApplicationContext();
        context.getBean("abc");
    }

    @Test
    public void getBeanDefinitionNamesWithEmptyContext() throws Exception {
        //given
        Context context = new ApplicationContext();

        //when
        String[] actual = context.getBeanDefinitionNames();

        //tnen
        String[] expected = {};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithOneBeanDefinition() throws Exception {
        String beanName = "FirstBean";
        Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>(){{
            put(beanName, new HashMap<String, Object>(){{
                put("type",Object.class);
            }});
        }};
        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {beanName};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithSeveralBeanDefinitions() throws Exception {
        String beanName1 = "FirstBean";
        String beanName2 = "SecondBean";
        Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>(){{
                put(beanName1, new HashMap<String, Object>(){{
                    put("type",Object.class);
                }});
                put(beanName2, new HashMap<String, Object>(){{
                    put("type",Object.class);
                }});
            }};
        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {beanName1, beanName2};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithEmptyBeanDefinition() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions = Collections.emptyMap();
        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {};
        assertArrayEquals(expected, actual);
    }

//    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void getBeanWithOneBeanDefinitionWithoutType() throws Exception {
        String beanName = "FirstBean";
        Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>(){{
            put(beanName, new HashMap<String, Object>(){{
//                put("type", null);
                put("isPrototype", false);
            }});
        }};
        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        Object bean = context.getBean(beanName);
    }

    @Test
    public void getBeanWithOneBeanDefinition() throws Exception {
        String beanName = "FirstBean";
        Class<TestBean> beanType = TestBean.class;
        //List<String> beanDescriptions = Arrays.asList(beanName);
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put(beanName,
                            new HashMap<String, Object>() {{
                                put("type", beanType);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean = (TestInterface) context.getBean(beanName);

        assertNotNull(bean);
    }

    @Test
    public void getBeanNotSameInstancesWithSameType() throws Exception {
        String beanName1 = "FirstBean";
        String beanName2 = "SecondBean";
        Class<TestBean> beanType = TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put(beanName1,
                            new HashMap<String, Object>() {{
                                put("type", beanType);
                            }}
                    );
                    put(beanName2,
                            new HashMap<String, Object>() {{
                                put("type", beanType);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean1 = (TestInterface) context.getBean(beanName1);
        TestInterface bean2 = (TestInterface) context.getBean(beanName2);

        assertNotSame(bean1, bean2);
    }

    @Test
    public void getBeanIsSingleton() throws Exception {
        String beanName = "FirstBean";
        Class<TestBean> beanType = TestBean.class;
        //List<String> beanDescriptions = Arrays.asList(beanName);
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put(beanName,
                            new HashMap<String, Object>() {{
                                put("type", beanType);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean1 = (TestInterface) context.getBean(beanName);
        TestInterface bean2 = (TestInterface) context.getBean(beanName);

        assertSame(bean1, bean2);
    }

    @Test
    public void getBeanIsPrototype() throws Exception {
        String beanName = "FirstBean";
        Class<TestBean> beanType = TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put(beanName,
                            new HashMap<String, Object>() {{
                                put("type", beanType);
                                put("isPrototype", true);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean1 = (TestInterface)context.getBean(beanName);
        TestInterface bean2 = (TestInterface) context.getBean(beanName);

        assertNotSame(bean1, bean2);
    }

    @Test
    public void getBeanWithOneDependedBean() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", false);
                            }}
                    );
                    put("testBeanWithConstructor",
                            new HashMap<String, Object>() {{
                                put("type", TestBeanWithConstructor.class);
                                put("isPrototype", false);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestBeanWithConstructor bean
                = (TestBeanWithConstructor) context.getBean("testBeanWithConstructor");

        assertNotNull(bean);
    }


    @Test
    public void getBeanWithSeveralDependedBeansIsSingleton() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", false);
                            }}
                    );
                    put("testBeanWithConstructorTwoParams",
                            new HashMap<String, Object>() {{
                                put("type", TestBeanWithConstructorTwoParams.class);
                                put("isPrototype", false);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestBeanWithConstructorTwoParams bean
                = (TestBeanWithConstructorTwoParams) context.getBean("testBeanWithConstructorTwoParams");

        assertNotNull(bean);
        assertSame(bean.testInterface, bean.testInterface1);
    }

    @Test
    public void getBeanWithSeveralDependedBeansIsPrototype() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }}
                    );
                    put("testBeanWithConstructorTwoParams",
                            new HashMap<String, Object>() {{
                                put("type", TestBeanWithConstructorTwoParams.class);
                                put("isPrototype", false);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestBeanWithConstructorTwoParams bean
                = (TestBeanWithConstructorTwoParams) context.getBean("testBeanWithConstructorTwoParams");

        assertNotNull(bean);
        assertNotSame(bean.testInterface, bean.testInterface1);
    }
    @Test
    public void getBeanCallInitMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean
                = (TestInterface) context.getBean("testInterface");

       assertSame("initialized", bean.initValue());
    }
    @Test
    public void getBeanPostConstructAnnotatedMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean
                = (TestInterface) context.getBean("testInterface");

        assertSame("initializedPostConstruct", bean.postConstructValue());
    }

    @Test
    public void getBeanPostConstructAnnotatedMethodBeforeInitMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("testInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        TestInterface bean
                = (TestInterface) context.getBean("testInterface");
        assertSame("initializedPostConstruct" , bean.getItemFromOrder());
    }

    static class TestBean implements TestInterface {
        public String initValue;
        public String postConstructValue;
        Queue<String> order = new LinkedList<>();

        public void init(){
            initValue = "initialized";
            order.add(initValue);
        }

        @Override
        public String initValue() {
            return initValue;
        }

        @MyPostConstruct
        public void postConstruct(){
            postConstructValue = "initializedPostConstruct";
            order.add(postConstructValue);
        }

        @Override
        public String postConstructValue() {
            return postConstructValue;
        }

        @Benchmark
        public String methodToBenchmark(String string){
            return new StringBuilder(string).reverse().toString();
        }

        @Override
        public String getItemFromOrder() {
            return order.poll();
        }
    }

    static class TestBeanWithConstructor  {
        public TestBeanWithConstructor(TestInterface testInterface) {
            this.testInterface = testInterface;
        }

        private final TestInterface testInterface;
    }

    static class TestBeanWithConstructorTwoParams  {
        public TestBeanWithConstructorTwoParams(TestInterface testInterface, TestInterface testInterface1) {
            this.testInterface = testInterface;
            this.testInterface1 = testInterface1;
        }

        public final TestInterface testInterface;
        public final TestInterface testInterface1;
    }

    interface TestInterface {
        void init();
        String initValue();
        void postConstruct();
        String postConstructValue();
        String methodToBenchmark(String string);
        String getItemFromOrder();
    }

}