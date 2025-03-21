package org.example.RPC.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.example.CustomUtil.CommonSerializer;
import org.example.CustomUtil.ServiceRegistry;
import org.example.CustomUtil.impl.CustomObjectDecoder;
import org.example.CustomUtil.impl.CustomObjectEncoder;
import org.example.POJO.RpcRequest;
import org.example.POJO.RpcResponse;
import org.example.RPC.Rpcs;

import java.io.IOException;
import java.lang.reflect.Method;

public class NettyRpcServer implements Rpcs {
    private final ServiceRegistry serviceRegistry;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private CommonSerializer serializer;
    public NettyRpcServer(ServiceRegistry serviceRegistry, int serializer_code) {
        this.serviceRegistry = serviceRegistry;
        this.serializer = CommonSerializer.getByCode(serializer_code);
    }
    public void start(int port) throws IOException {
        // 创建 bossGroup 客户端连接和 workerGroup 读写
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            // 创建 ServerBootstrap 实例用于启动服务端
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // 指定使用 NioServerSocketChannel 作为通道类型
                    .channel(NioServerSocketChannel.class)
                    // 配置 ChannelInitializer，用于初始化每个新连接的 ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new CustomObjectDecoder());
                            ch.pipeline().addLast(new CustomObjectEncoder(serializer));
                            ch.pipeline().addLast(new NettyRpcServerHandler());
                        }
                    })
                    // 设置服务端接收连接的队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置客户端连接的 TCP 保持活动状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并开始接收连接
            ChannelFuture f = b.bind(port).sync();

            System.out.println("Netty RPC Server started and listening on port " + port);

            // 等待服务器关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // 处理线程中断异常
            Thread.currentThread().interrupt();
        } finally {
            // 优雅地关闭 EventLoopGroup
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
            try {
                System.out.println("服务器接收到请求: "+rpcRequest);
                String interfaceName = rpcRequest.getInterfaceName();
                System.out.println("服务器接收到请求接口名: "+interfaceName);
                // 查找服务
                Class<?> serviceImplClass = null;
                if (rpcRequest != null) {
                    serviceImplClass = serviceRegistry.getServiceImplClass(rpcRequest.getInterfaceName());
                }
                // 执行方法调用
                if (serviceImplClass == null) {
                    return;
                }
                Object service = serviceImplClass.getDeclaredConstructor().newInstance();

                Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                Object[] parameters = rpcRequest.getParameters();
                System.out.println("请求的方法实际参数值:");
                for (Object param : parameters) {
                    if (param != null) {
                        System.out.println("参数类型: " + param.getClass().getName() + ", 参数值: " + param);
                    } else {
                        System.out.println("参数值为 null");
                    }
                }

                // 输出 rpcRequest.getParameters() 中每个参数的类型
                Object[] parameterss = rpcRequest.getParameters();
                if (parameterss != null) {
                    System.out.println("请求参数类型信息：");
                    for (int i = 0; i < parameterss.length; i++) {
                        Object param = parameterss[i];
                        if (param != null) {
                            System.out.println("参数 " + (i + 1) + " 的类型: " + param.getClass().getName());
                        } else {
                            System.out.println("参数 " + (i + 1) + " 为 null");
                        }
                    }
                } else {
                    System.out.println("请求参数为空");
                }

                // 反射调用
//                Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                Object result = method.invoke(service, rpcRequest.getParameters());
                System.out.println("[server] " + result);

//                Object result = requestHandler.handle(msg, service);
                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
                future.addListener(f -> {
                    if (f.isSuccess()) {
                        // 操作成功，关闭 Channel
                        System.out.println("操作成功");
                        ctx.channel().close();
                    } else {
                        // 操作失败，记录异常信息
                        System.err.println("写入数据失败: " + f.cause());
                        ctx.channel().close();
                    }
                });
            } finally {
                ReferenceCountUtil.release(rpcRequest);
            }
        }
    }
}
