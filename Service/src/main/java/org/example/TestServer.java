package org.example;

public class TestServer {
    public static void main(String[] args) {
        // 注册服务
        ServiceRegistry.registerService("org.example.HelloService", org.example.HelloServiceImpl.class);

        RpcServer rpcServer = new RpcServer();
        rpcServer.work(9000);
    }
}
