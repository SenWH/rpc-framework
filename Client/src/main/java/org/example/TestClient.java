package org.example;

import org.example.POJO.HelloObject;
import org.example.Proxy.RpcClientProxy;
import org.example.Service.HelloService;

public class TestClient {
    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject("Xiao Ming");
        String result = helloService.sayHello(object);

        System.out.println(result);
    }
}
