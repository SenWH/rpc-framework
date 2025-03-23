package org.example.RPC;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

public interface Rpcs {
    // 启动服务的方法
    void start() throws IOException;

    // 处理新连接的方法
    default void handleAccept(SelectionKey key) throws IOException {

    }

    // 处理读事件的方法
    default void handleRead(SelectionKey key) {

    }

    // 处理 RPC 请求的方法
    default void handleRequest(java.nio.channels.SocketChannel socketChannel, byte[] data) {

    }

    // 停止服务的方法
    default void stop() {

    }

    // 获取线程池的方法
    default ExecutorService getThreadPool() {
        return null;
    }

    //向Nacos注册服务
    <T> void publishService(Class<T> serviceClass);
}
