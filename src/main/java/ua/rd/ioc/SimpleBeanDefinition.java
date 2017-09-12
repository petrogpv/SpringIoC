package ua.rd.ioc;

public class SimpleBeanDefinition implements BeanDefinition {

    private final String beanName;
    private final Class<?> beanType;
    private final boolean isPrototype;

    public SimpleBeanDefinition(String beanName, Class<?> beanType, boolean isPrototype) {
        this.beanName = beanName;
        this.beanType = beanType;
        this.isPrototype = isPrototype;
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    @Override
    public Class<?> getBeanType() {
        return beanType;
    }

    @Override
    public boolean isPrototype() {
        return isPrototype;
    }
}
