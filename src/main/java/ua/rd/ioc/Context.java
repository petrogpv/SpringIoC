package ua.rd.ioc;

public interface Context {
    Object getBean(String beanName);
    String[] getBeanDefinitionNames();
}
