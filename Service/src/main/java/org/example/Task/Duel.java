package org.example.Task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.CustomUtil.ServiceRegistry;
import org.example.POJO.RpcRequest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Duel implements Runnable {
    private ServiceRegistry serviceRegistry;
    private Socket socket;
    private static final Logger logger = LoggerFactory.getLogger(Duel.class);
    private SocketChannel socketChannel;
    private byte[] data;
    private static final ObjectMapper objectMapper = new ObjectMapper();

//    public Task(Socket socket, ServiceRegistry serviceRegistry) {
//        this.socket = socket;
//        this.serviceRegistry = serviceRegistry;
//    }

    public Duel(SocketChannel socketChannel, ServiceRegistry serviceRegistry, byte[] data ) {
        this.socketChannel = socketChannel;
        this.serviceRegistry = serviceRegistry;
        this.data = data;
    }

    public void run() {
        try {
            synchronized (socketChannel) { // 确保线程安全
                if (!socketChannel.isOpen()) {
                    return; // 如果通道已关闭，则直接退出
                }
                // 解析请求
                RpcRequest rpcRequest = parseRequest(data);
                // 查找服务
                Class<?> serviceImplClass = null;
                if (rpcRequest != null) {
                    serviceImplClass = serviceRegistry.getServiceImplClass(rpcRequest.getInterfaceName());
                }
                // 执行方法调用
                if (serviceImplClass == null) {
                    logger.error("未找到服务实现类: {}", rpcRequest.getInterfaceName());
                    return;
                }
                // 创建服务实例
                Object service = serviceImplClass.getDeclaredConstructor().newInstance();

                // 反射调用
                Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                Object result = method.invoke(service, rpcRequest.getParameters());
                // 发送响应
                sendResponse(socketChannel, result);
            }
        } catch (Exception e) {
            logger.error("处理请求时出错", e);
            try {
                socketChannel.close();
            } catch (IOException ex) {
                logger.error("关闭连接时出错", ex);
            }
        }
    }

    private void sendResponse(SocketChannel socketChannel, Object result) {
        try {
            // 将结果序列化为 JSON 字符串
            String jsonResult = objectMapper.writeValueAsString(result);
            // 将 JSON 字符串转换为字节数组
            byte[] data = jsonResult.getBytes();
            // 创建 ByteBuffer 并包装字节数组
            ByteBuffer buffer = ByteBuffer.wrap(data);
            // 通过 SocketChannel 发送数据
            socketChannel.write(buffer);
            System.out.println("发送响应");
        } catch (IOException e) {
            logger.error("发送响应时出错", e);
        }
    }

    private RpcRequest parseRequest(byte[] data) {
        try {
            return objectMapper.readValue(data, RpcRequest.class);
        } catch (Exception e) {
            logger.error("解析请求数据时出错", e);
            return null;
        }
    }
//    public void run() {
//        try {
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            // 接收请求
//            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
//            String serviceName = rpcRequest.getInterfaceName();
//            Class<?> serviceImplClass = serviceRegistry.getServiceImplClass(serviceName);
//            if (serviceImplClass == null) {
//                logger.error("未找到服务实现类: {}", serviceName);
//                return;
//            }
//            // 创建服务实例
//            Object service = serviceImplClass.getDeclaredConstructor().newInstance();
//
//            // 反射调用
//            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
//            Object result = method.invoke(service, rpcRequest.getParameters());
//            // 发送响应
//            objectOutputStream.writeObject(result);
//            objectOutputStream.flush();
//
//        } catch (IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
//                 InvocationTargetException | InstantiationException e) {
//            logger.error("请求处理失败", e);
//        }
//    }
}
