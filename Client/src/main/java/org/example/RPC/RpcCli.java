package org.example.RPC;

import org.example.POJO.RpcRequest;

public interface RpcCli {
    /**
     * 发送 RPC 请求
     * @param host 服务器主机名
     * @param port 服务器端口号
     * @param rpcRequest RPC 请求对象
     * @return 服务器响应结果
     */
    Object sendRequest(String host, int port, RpcRequest rpcRequest);
}
