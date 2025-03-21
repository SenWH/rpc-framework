package org.example.CustomUtil;

public interface ServiceRegistry {
    void register(Class<?> serviceImplClass);
    Class<?> getServiceImplClass(String serviceName);
}
