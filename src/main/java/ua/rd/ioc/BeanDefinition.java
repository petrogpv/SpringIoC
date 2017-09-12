package ua.rd.ioc;

public interface BeanDefinition {
    String getBeanName();

    Class<?> getBeanType();

    boolean isPrototype();
}
