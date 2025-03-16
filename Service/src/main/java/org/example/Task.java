package org.example;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class Task implements Runnable {
    private Socket socket;
    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 接收请求
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String serviceName = rpcRequest.getServiceName();
            Class<?> serviceImplClass = ServiceRegistry.getServiceImplClass(serviceName);
            if (serviceImplClass == null) {
                logger.error("未找到服务实现类: {}", serviceName);
                return;
            }
            // 创建服务实例
            Object service = serviceImplClass.getDeclaredConstructor().newInstance();

            // 反射调用
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object result = method.invoke(service, rpcRequest.getParameters());
            // 发送响应
            objectOutputStream.writeObject(result);
            objectOutputStream.flush();

        } catch (IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException | InstantiationException e) {
            logger.error("请求处理失败", e);
        }
    }
}
