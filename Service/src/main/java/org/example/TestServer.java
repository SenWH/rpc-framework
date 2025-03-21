package org.example;

import org.example.Common.SerializerCode;
import org.example.CustomUtil.ServiceRegistry;
import org.example.CustomUtil.impl.ServiceRegistryImpl;
import org.example.RPC.Rpcs;
import org.example.RPC.impl.NettyRpcServer;
import org.example.RPC.impl.NioRpcServer;
import org.example.Service.impl.HelloServiceImpl;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) throws IOException {
        ServiceRegistry serviceRegistry = new ServiceRegistryImpl();
        serviceRegistry.register(HelloServiceImpl.class);
        Rpcs rpcServer = new NettyRpcServer(serviceRegistry, SerializerCode.JSON);
        rpcServer.start(9000);

    }
}
