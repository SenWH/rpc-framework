---
title: 造轮子：RPC框架
date: 2025-03-17 00:22:47
tags:

---

# 客户端-服务端 v1.0

## 客户端

1. 调用服务接口（客户端无实现类）
2. 获取接口**Class对象**，使用动态代理**为接口生成代理对象**

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



# 客户端-服务端 v1.1

## 待优化问题

1. 服务端用信息什么注册？

- 现在服务端维护一个注册表，通过HashMap记录（服务全限定名，服务Class对象）。即根据收到的服务全限定名获取服务。使用户只关心接口->注册接口名作为服务名注册

> ```java
> Class<?>[] interfaces = serviceImplClass.getInterfaces();
> for (Class<?> intf : interfaces) {
>  // 使用接口的规范名称作为服务名
>  String serviceName = intf.getCanonicalName();
>  if(serviceMap.containsKey(serviceName)){
>      return;
>  }
>  // 将服务名称和对应的服务实现类的 Class 对象存入 map
>  serviceMap.put(serviceName, serviceImplClass);
> }
> ```

2. 若有多个RPC服务器，服务如何注册？

- 现在所有服务使用同一个实例对象，注册方法设置为静态方法，多个RPC服务若注册则都放同一个实例，高耦合。->取消静态方法，注册表实例化对象作为参数传入RPC服务

> ```java
> ServiceRegistry serviceRegistry = new ServiceRegistryImpl();
> serviceRegistry.register(HelloServiceImpl.class);
> RpcServer rpcServer = new RpcServer(serviceRegistry);
> ```

3. 此时传输采用的是BIO的方式，如何改成性能更好的NIO？

> ```java
> import java.io.ObjectInputStream;
> import java.io.ObjectOutputStream;
> ```

***

- 使用Java nio库+jackson序列化实现NIO
  - 如果直接转成字节数组发送，在解析时头昏脑胀->也就是说要一个字符一个字符读取切割

> 序列化登场: 对象的状态信息转换为可以存储或传输的形式（如字节序列、JSON 字符串等）的过程
>
> ```java
> String jsonRequest = objectMapper.writeValueAsString(rpcRequest);
> ```

实际上：在BIO也用了序列化反序列化的过程-如果一个类实现了 `Serializable` 接口，在使用 `ObjectOutputStream`（属于 BIO 体系）时，该类的对象就能被自动序列化并且自动转换为字节流。

- 使用其序列化的时候遇到问题：RpcRequest中的parameters 是Object[] 类型-> JSON 序列化时，`Object` 类型的字段会被序列化为一个通用的 JSON 结构，**反序列化时，Jackson 默认会将其解析`LinkedHashMap`**---**即多态类型处理问题**

> ```java
> 解决方案,字段前加入注解：@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class") 使用类的全限定名作为类型标识/将类型信息作为 JSON 对象的一个属性存储/指定类型信息的属性名为 @class
> ```

- 收发通过channel->将通道注册到选择器中->底层 （fds描述符注册到epoll）

> ```java
> // 打开选择器 --创建红黑树实例
> selector = Selector.open(); 
> // 打开服务器套接字通道
> serverSocketChannel = ServerSocketChannel.open();
> // 绑定端口
> serverSocketChannel.socket().bind(new InetSocketAddress(port));
> // 设置为非阻塞模式
> serverSocketChannel.configureBlocking(false);
> // 注册 OP_ACCEPT 事件到选择器 描述符（FD）添加到红黑树
> serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
> ```

- 当监听到请求，遍历事件并执行（当文件描述符（如套接字）的状态发生变化（如数据可读、可写等），内核会检测到这些事件。对于epoll，内核会调用一个回调函数（如`ep_poll_callback`），将事件通知给epoll实例。这个回调函数会将事件添加到epoll的就绪队列（`rdllist`）中，用户态程序调用`epoll_wait`检查就绪队列并取出）

> ```java
> selector.select(); 
> // 获取所有发生的事件 返回链表集合
> Set<SelectionKey> selectedKeys = selector.selectedKeys()
> ```

***

该版本实际上实现了一个单Reactor模型，单线程派发任务，多线程分别处理连接和执行读/写操作

# 客户端-服务端 v1.2

## 待优化问题

1. NIO 有空轮询Bug：事件返回数量为0，但是应该阻塞的selector.select()不断被唤醒，导致CPU100%

- 原因：当一个连接被突然中断，epoll会将事件集合置位，由于事件集合发生了变化，`Selector`被唤醒

2. 单Reactor模型在高并发场景有性能瓶颈->多线程多Reactor模型



用Netty网络编程框架，封装Select处理细节

> 责任链模式，数据入站->找重写了decoder方法->链顺序移动到下一个Handler（ChannelInboundHandler）专门处理入栈数据
>
> ```java
> bootstrap = new Bootstrap();
> bootstrap.group(group) //group 线程池从中取EventLoop （线程）执行具体任务
>      .channel(NioSocketChannel.class) //Channel被注册 Selector 监听事件 
>      .handler(new ChannelInitializer<SocketChannel>() {
>          @Overrid //当有就绪事件调用channel read方法读取数据进入责任链
>          protected void initChannel(SocketChannel ch) throws Exception {
>              ch.pipeline().addLast(new CustomObjectDecoder());
>              ch.pipeline().addLast(new CustomObjectEncoder(serializer));
>              ch.pipeline().addLast(new NettyRpcClientHandler()); //channelRead0 触发
>          }
>      });
> 
> ctx.fireChannelRead(processedMsg); //传递给下一个入站方向的处理器
> ```

> 出站则责任链逆序（ChannelOutboundHandler）->找重写了encoder方法->出站
>
> ```java
> ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result)); //将处理结果writeAndFlush 写入出站方向-传递给下一个处理器
> ```

> `ChannelHandlerContext` 提供了 `ChannelHandler` 与 `ChannelPipeline` 以及其他组件之间的交互能力
>
> ```java
> protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
>  System.out.println("收到消息:" + response);
>  AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
>  ctx.channel().attr(key).set(response); //设置响应对象名
> }
> ```

自定义序列化器->封装协议号/序列器类型号/包类型/数据长度+数据

> ```java
> CustomObjectEncoder extends MessageToByteEncoder //实现该接口，当数据离开时自动调用encode方法
> CustomObjectDecoder extends ByteToMessageDecoder //实现该接口，当数据入站时自动调用decode方法
> ```

针对前面所提：对Object序列化时的 多态类型处理问题->除了用注解方式，还可以用强转：

> ```java
> for(int i = 0; i < rpcRequest.getParamTypes().length; i ++) {
>     Class<?> clazz = rpcRequest.getParamTypes()[i];
>     if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
>         byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
>         rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
>     }
> }
> ```
