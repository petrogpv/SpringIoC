package ua.rd.ioc;

public interface Config {

    BeanDefinition[] EMPTY_BEANDEFINITION = new BeanDefinition[0];

    BeanDefinition[] beanDefinitions();
}
