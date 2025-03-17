package org.example;

public class TestServer {
    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = new ServiceRegistryImpl();
        serviceRegistry.register(HelloServiceImpl.class);
        RpcServer rpcServer = new RpcServer(serviceRegistry);
        rpcServer.start(9000);
    }
}
