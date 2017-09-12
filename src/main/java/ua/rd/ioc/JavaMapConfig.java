package ua.rd.ioc;

import java.util.List;
import java.util.Map;

public class JavaMapConfig implements Config {

    private Map<String, Map<String, Object>> beanDescriptions;

    public JavaMapConfig(Map<String, Map<String, Object>> beanDescriptions) {
        this.beanDescriptions = beanDescriptions;
    }

    private BeanDefinition beanDefinition(
            Map.Entry<String, Map<String, Object>> descriptionEntry) {
        return new SimpleBeanDefinition(
                descriptionEntry.getKey(),
                (Class<?>) descriptionEntry.getValue().get("type"),
                (boolean) descriptionEntry.getValue().getOrDefault("isPrototype", false)
        );

    }

    @Override
    public BeanDefinition[] beanDefinitions() {
        BeanDefinition[] beanDefinitions =
                beanDescriptions.entrySet().stream()
                        .map(this::beanDefinition)
                        .toArray(BeanDefinition[]::new);

        return beanDefinitions;
    }
}
