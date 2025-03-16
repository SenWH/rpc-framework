---
title: 造轮子：RPC框架
date: 2025-03-17 00:22:47
tags:

---

# 客户端-服务端

## 客户端

1. 调用服务接口（客户端无实现类）
2. 获取接口**Class对象**，使用动态代理为接口生成代理对象

> ```java
> HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
> ```

3. 重写代理类invoke方法（在调用接口方法时，代理对象拦截请求，调用invoke方法）

> ```java
> invoke(Object proxy, Method method, Object[] args)
> ```

4. 在Invoke方法中，获取所调用的方法信息（接口名、方法名、参数信息）封装成请求信息
5. 通过Socket建立连接并通过OutputStream发送
6. 通过Socket获取InputStream返回响应



## 服务端

1. Socket监听端口，获取请求数据

2. 创建线程池，处理Socket监听到的请求

> ```java
> threadPool.execute(new Task(socket, helloService));
> public class Task implements Runnable
> ```

3. 解析请求体里的数据，根据注册映射表（通过请求的接口全限定名映射实现类），获取实现类实例

> ```java
> Object service = serviceImplClass.getDeclaredConstructor().newInstance();
> ```

3. 获取方法信息，反射调用服务

> ```java
> Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
> Object result = method.invoke(service, rpcRequest.getParameters());
> ```

4. 通过Socket返回响应数据

