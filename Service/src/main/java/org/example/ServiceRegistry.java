package org.example;

public interface ServiceRegistry {
    void register(Class<?> serviceImplClass);
    Class<?> getServiceImplClass(String serviceName);
}
