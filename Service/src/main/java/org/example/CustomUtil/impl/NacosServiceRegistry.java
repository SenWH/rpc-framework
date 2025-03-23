package org.example.CustomUtil.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.CustomUtil.ServiceRegistry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static NamingService namingService;

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生: ", e);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        if (namingService == null) {
            logger.error("Nacos服务未成功连接，无法查找服务");
            return null;
        }
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName);
            if (instances.isEmpty()) {
                logger.error("未找到服务: {}", serviceName);
                return null;
            }
            // 选择负载均衡策略，这里可以根据需要修改
            Instance selectedInstance = randomLoadBalance(instances);
            // Instance selectedInstance = roundRobinLoadBalance(instances);
            // Instance selectedInstance = weightedRandomLoadBalance(instances);

            return new InetSocketAddress(selectedInstance.getIp(), selectedInstance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生:", e);
        }
        return null;
    }
    /**
     * 随机负载均衡策略
     * @param instances 服务实例列表
     * @return 选中的服务实例
     */
    private Instance randomLoadBalance(List<Instance> instances) {
        Random random = new Random();
        return instances.get(random.nextInt(instances.size()));
    }

    // 用于轮询策略的计数器
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    /**
     * 轮询负载均衡策略
     * @param instances 服务实例列表
     * @return 选中的服务实例
     */
    private Instance roundRobinLoadBalance(List<Instance> instances) {
        int index = roundRobinIndex.getAndIncrement() % instances.size();
        return instances.get(index);
    }

    /**
     * 加权随机负载均衡策略
     * @param instances 服务实例列表
     * @return 选中的服务实例
     */
    private Instance weightedRandomLoadBalance(List<Instance> instances) {
        int totalWeight = 0;
        for (Instance instance : instances) {
            totalWeight += instance.getWeight();
        }
        Random random = new Random();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (Instance instance : instances) {
            currentWeight += instance.getWeight();
            if (currentWeight > randomWeight) {
                return instance;
            }
        }
        // 理论上不会执行到这里
        return instances.get(0);
    }
}
