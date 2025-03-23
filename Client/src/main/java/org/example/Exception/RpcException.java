package org.example.Exception;

// 自定义 RPC 异常类，继承自 RuntimeException
public class RpcException extends RuntimeException {

    // 构造函数，接收 RpcError 枚举作为参数
    public RpcException(RpcError error) {
        // 调用父类 RuntimeException 的构造函数，传入错误信息
        super(error.getMessage());
    }
}
