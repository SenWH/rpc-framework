package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private final int CORE_POOL_SIZE = 10;
    private final int MAXIMUM_POOL_SIZE = 100;
    private final long KEEP_ALIVE_TIME = 10000;

    private final ExecutorService threadPool;
    private final ServiceRegistry serviceRegistry;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, workQueue, threadFactory, handler);
    }

    public void start(int port) {
        try {
            // 打开选择器
            selector = Selector.open();
            // 打开服务器套接字通道
            serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            // 设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 注册 OP_ACCEPT 事件到选择器
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                // 等待事件发生
                selector.select();
                // 获取所有发生的事件
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        // 处理新的连接
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        // 处理读事件
                        handleRead(key);
                    }
                    // 移除已处理的事件
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            logger.error("服务启动失败", e);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        // 接受新的连接
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        // 注册 OP_READ 事件到选择器
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) {
        threadPool.submit(() -> {
            if (!key.isValid()) {
                logger.warn("无效的 SelectionKey，跳过处理");
                return;
            }
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            try {
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    // 处理接收到的数据
                    handleRequest(socketChannel, data);
                } else if (bytesRead == -1) {
                    // 客户端关闭连接
                    key.cancel();
                    socketChannel.close();
                }
            } catch (IOException e) {
                try {
                    key.cancel();
                    socketChannel.close();
                    logger.error("处理读事件时出错", e);
                } catch (IOException ex) {
                    logger.error("关闭连接时出错", ex);
                }
            }
        });
    }

    private void handleRequest(SocketChannel socketChannel, byte[] data) {
        // 处理 RPC 请求的逻辑
        Duel task = new Duel(socketChannel, serviceRegistry, data);
        threadPool.submit(task);
    }

    public void stop() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
        } catch (IOException e) {
            logger.error("关闭资源时出错", e);
        }
    }
//    public void start(int port) {
//        try {
//            ServerSocket serverSocket = new ServerSocket(port);
//            logger.warn("服务器启动:{}", port);
//            Socket socket;
//            while ((socket = serverSocket.accept()) != null) {
//                logger.warn("客户端连接:{}", socket);
//                threadPool.execute(new Task(socket,serviceRegistry));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
