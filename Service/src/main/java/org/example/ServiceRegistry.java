package org.example;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private static final Map<String, Class<?>> serviceMap = new HashMap<>();

    public static void registerService(String serviceName, Class<?> serviceImplClass) {
        serviceMap.put(serviceName, serviceImplClass);
    }

    public static Class<?> getServiceImplClass(String serviceName) {
        return serviceMap.get(serviceName);
    }
}
