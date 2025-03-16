package org.example;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {
    private Integer status;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatus(200);
        response.setData(data);
        return response;
    }
    public static <T> RpcResponse<T> fail(String message){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatus(500);
        response.setMessage(message);
        return response;
    }
}
