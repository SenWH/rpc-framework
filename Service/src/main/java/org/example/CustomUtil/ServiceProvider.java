package org.example.CustomUtil;

//本地注册表
public interface ServiceProvider {
        void register(Class<?> serviceImplClass);
        Class<?> getServiceImplClass(String serviceName);
}
