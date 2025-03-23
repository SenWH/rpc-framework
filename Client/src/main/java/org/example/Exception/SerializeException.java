package org.example.Exception;

// 自定义序列化异常类，继承自 RuntimeException
public class SerializeException extends RuntimeException {

    // 构造函数，接收一个错误信息作为参数
    public SerializeException(String message) {
        // 调用父类 RuntimeException 的构造函数，传入错误信息
        super(message);
    }
}
