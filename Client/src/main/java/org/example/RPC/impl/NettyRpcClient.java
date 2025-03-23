package org.example.RPC.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import org.example.CustomUtil.CommonSerializer;
import org.example.CustomUtil.impl.CustomObjectDecoder;
import org.example.CustomUtil.impl.CustomObjectEncoder;
import org.example.POJO.RpcRequest;
import org.example.POJO.RpcResponse;
import org.example.RPC.RpcCli;

import java.util.concurrent.CompletableFuture;
public class NettyRpcClient implements RpcCli {

    private final EventLoopGroup group = new NioEventLoopGroup(); //喊线程池
    private final Bootstrap bootstrap;
    private CommonSerializer serializer;

    public NettyRpcClient(int serializer_code) {
        this.serializer = CommonSerializer.getByCode(serializer_code);
        bootstrap = new Bootstrap();
        bootstrap.group(group) //group 线程池从中取EventLoop （线程）执行具体任务
                .channel(NioSocketChannel.class) //NioSocketChannel底层使用Selector/SocketChannel实现NIO
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new CustomObjectDecoder());
                        ch.pipeline().addLast(new CustomObjectEncoder(serializer));
                        ch.pipeline().addLast(new NettyRpcClientHandler()); //channelRead0 触发
                    } //
                });
    }
    @Override
    public Object sendRequest(String host, int port, RpcRequest rpcRequest) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();//阻塞线程
            Channel channel = channelFuture.channel(); //获取已连接channel对象
            channel.writeAndFlush(rpcRequest); //编码器序列化为字节数组放入ByteBuf中通过网络发送

            channel.closeFuture().sync();  //阻塞线程直到channel关闭
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            RpcResponse rpcResponse = channel.attr(key).get();
            return rpcResponse.getData();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

        private static class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
                System.out.println("收到消息:" + response);
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse"); //创建键名用于放属性
                ctx.channel().attr(key).set(response);
                ctx.channel().close();
            }
        }

    public void close() {
        group.shutdownGracefully();
    }
}
