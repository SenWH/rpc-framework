package org.example;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private final ExecutorService threadPool;

    public RpcServer() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = 100;
        long keepAliveTime = 10000;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, workQueue, threadFactory, handler);
    }

    public void work(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logger.warn("服务器启动:{}", port);
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.warn("客户端连接:{}", socket);
                threadPool.execute(new Task(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
