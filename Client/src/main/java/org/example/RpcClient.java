package org.example;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRequest(String host, int port, RpcRequest rpcRequest) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            // 发送请求
            outputStream.writeObject(rpcRequest);
            outputStream.flush();

            // 接收响应
            return inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("请求发送失败", e);
            return null;
        }
    }
}
