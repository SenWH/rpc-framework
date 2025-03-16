package org.example;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 客户端无法直接调用服务端的方法，需要通过代理对象来调用
 * 代理对象需要实现 InvocationHandler 接口，并重写 invoke 方法
 * 代理对象的 invoke 方法中，会将请求发送给服务端，然后等待服务端返回结果
 * 代理对象的 invoke 方法中，会将服务端返回的结果转换为客户端需要的类型，然后返回给客户端
 */
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;
    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
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
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient client = new RpcClient();
        // 发送请求给服务端
        return client.sendRequest(host, port, rpcRequest);
    }

    public HelloService getProxy(Class<HelloService> helloServiceClass) {
        return (HelloService) java.lang.reflect.Proxy.newProxyInstance(helloServiceClass.getClassLoader(), new Class<?>[]{helloServiceClass}, this);
    }
}
