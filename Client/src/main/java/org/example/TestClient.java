package org.example;

import org.example.CustomUtil.ServiceRegistry;
import org.example.CustomUtil.impl.NacosServiceRegistry;
import org.example.POJO.HelloObject;
import org.example.Proxy.RpcClientProxy;
import org.example.Service.HelloService;

import java.net.InetSocketAddress;

public class TestClient {
    public static void main(String[] args) {
        //取服务地址
        ServiceRegistry serviceRegistry = new NacosServiceRegistry();
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(HelloService.class.getCanonicalName());
        RpcClientProxy rpcClientProxy = new RpcClientProxy(inetSocketAddress);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject("Xiao Ming");
        String result = helloService.sayHello(object);
        System.out.println(result);
    }
}
