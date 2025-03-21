package org.example.RPC.impl;

import org.example.POJO.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestHandler {
    Object handle(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 反射调用
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        Object result = method.invoke(service, rpcRequest.getParameters());
        System.out.println("[server] " + result);
        return result;
    }
}
