package org.example.CustomUtil.impl;

import org.example.CustomUtil.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistryImpl implements ServiceRegistry {

    // 用于存储服务名称和对应的服务实现类的映射
    private final Map<String, Class<?>> serviceMap = new HashMap<>();

    /**
     * 注册服务实现类
     * @param serviceImplClass 服务实现类的 Class 对象
     */
    @Override
    public void register(Class<?> serviceImplClass) {
        // 获取服务实现类实现的所有接口
        Class<?>[] interfaces = serviceImplClass.getInterfaces();
        for (Class<?> intf : interfaces) {
            // 使用接口的规范名称作为服务名
            String serviceName = intf.getCanonicalName();
            if(serviceMap.containsKey(serviceName)){
                return;
            }
            // 将服务名称和对应的服务实现类的 Class 对象存入 map
            serviceMap.put(serviceName, serviceImplClass);
        }
    }

    /**
     * 根据服务名称获取服务实现类的 Class 对象
     * @param serviceName 服务名称
     * @return 服务实现类的 Class 对象，如果未找到则返回 null
     */
    @Override
    public Class<?> getServiceImplClass(String serviceName) {
        // 从 map 中获取对应的服务实现类的 Class 对象
        return serviceMap.get(serviceName);
    }
}
