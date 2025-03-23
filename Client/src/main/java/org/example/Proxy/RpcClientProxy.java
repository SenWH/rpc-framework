package org.example.Proxy;

import org.example.Common.SerializerCode;
import org.example.POJO.RpcRequest;
import org.example.RPC.RpcCli;
import org.example.RPC.impl.NettyRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 客户端无法直接调用服务端的方法，需要通过代理对象来调用
 * 代理对象需要实现 InvocationHandler 接口，并重写 invoke 方法
 * 代理对象的 invoke 方法中，会将请求发送给服务端，然后等待服务端返回结果
 * 代理对象的 invoke 方法中，会将服务端返回的结果转换为客户端需要的类型，然后返回给客户端
 */
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;
    public RpcClientProxy(InetSocketAddress inetSocketAddress) {
        this.host = inetSocketAddress.getHostName();
        this.port = inetSocketAddress.getPort();
    }

    /**
     * 代理对象的 invoke 方法中，会将请求发送给服务端，然后等待服务端返回结果
     * 代理对象的 invoke 方法中，会将服务端返回的结果转换为客户端需要的类型，然后返回给客户端
     * @param proxy 代理对象
     * @param method 调用的方法
     * @param args 方法的参数
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 创建一个 RpcRequest 对象，将请求信息封装到 RpcRequest 对象中
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcCli client = new NettyRpcClient(SerializerCode.KRYO); // 序列化方式
        // 发送请求给服务端

        // 阻塞等待结果
        return client.sendRequest(host, port, rpcRequest);

    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz},this);
    }
}
