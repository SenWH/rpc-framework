package org.example;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    public Object sendRequest(String host, int port, RpcRequest rpcRequest) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));
            while (!socketChannel.finishConnect()) {
                System.out.println("等待连接...");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequest = objectMapper.writeValueAsString(rpcRequest);
            byte[] data = jsonRequest.getBytes();

            ByteBuffer wrap = ByteBuffer.wrap(data);
            socketChannel.write(wrap);

            // 接收响应
            ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
            int bytesRead;
            int maxRetries = 5; // 最大重试次数
            int retryInterval = 1000; // 重试间隔（毫秒）
            int retries = 0;

            while (retries < maxRetries) {
                bytesRead = socketChannel.read(responseBuffer);
                System.out.println("bytesRead:" + bytesRead);
                if (bytesRead > 0) {
                    responseBuffer.flip();
                    byte[] responseBytes = new byte[responseBuffer.remaining()];
                    responseBuffer.get(responseBytes);
                    return new String(responseBytes);
                }
                try {
                    // 等待一段时间后再次尝试读取
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retries++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    // BIO
//    public Object sendRequest(String host, int port, RpcRequest rpcRequest) {
//        try (Socket socket = new Socket(host, port);
//             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
//            // 发送请求
//            outputStream.writeObject(rpcRequest);
//            outputStream.flush();
//
//            // 接收响应
//            return inputStream.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            logger.error("请求发送失败", e);
//            return null;
//        }
//    }
}
