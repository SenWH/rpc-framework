package org.example.Exception;

// 定义 RPC 错误的枚举类
public enum RpcError {
    // 序列化器未找到的错误
    SERIALIZER_NOT_FOUND("未找到合适的序列化器"),
    SERVICE_REGISTRATION_FAILED("服务注册失败");
    // 错误信息
    private final String message;

    // 构造函数，接收错误信息作为参数
    RpcError(String message) {
        this.message = message;
    }

    // 获取错误信息的方法
    public String getMessage() {
        return message;
    }
}
