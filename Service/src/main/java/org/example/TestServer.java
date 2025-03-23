package org.example;

import org.example.Common.SerializerCode;
import org.example.CustomUtil.ServiceProvider;
import org.example.CustomUtil.ServiceRegistry;
import org.example.CustomUtil.impl.ServiceProviderImpl;
import org.example.RPC.Rpcs;
import org.example.RPC.impl.NettyRpcServer;
import org.example.RPC.impl.NioRpcServer;
import org.example.Service.HelloService;
import org.example.Service.impl.HelloServiceImpl;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) throws IOException {
        Rpcs rpcServer = new NettyRpcServer(SerializerCode.KRYO, "127.0.0.1",9000);
        rpcServer.publishService(HelloServiceImpl.class);
        rpcServer.start();
    }
}
