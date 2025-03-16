package org.example;

public class TestClient {
    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject("Xiao Ming");
        String result = helloService.sayHello(object);
        System.out.println(result);
    }
}
