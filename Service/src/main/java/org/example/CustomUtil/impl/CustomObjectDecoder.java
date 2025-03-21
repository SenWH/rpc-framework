package org.example.CustomUtil.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.Common.PackageType;
import org.example.CustomUtil.CommonSerializer;
import org.example.POJO.RpcRequest;
import org.example.POJO.RpcResponse;

import java.util.List;

// 自定义解码器，处理粘包问题并反序列化对象
public class CustomObjectDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 确保有足够的数据读取包类型、序列化器代码和数据长度
        if (byteBuf.readableBytes() < 12) {
            return;
        }
        // 标记当前读取位置
        byteBuf.markReaderIndex();

        // 读取协议类型
        int ArgType = byteBuf.readInt();
        // 读取包类型
        int packageType = byteBuf.readInt();
        // 读取序列化器代码
        int serializerCode = byteBuf.readInt();
        // 读取数据长度
        int dataLength = byteBuf.readInt();

        // 确保有足够的数据读取对象数据
        if (byteBuf.readableBytes() < dataLength) {
            // 数据不足，重置读取位置
            byteBuf.resetReaderIndex();
            return;
        }

        // 读取对象数据
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        // 根据序列化器代码获取对应的序列化器
        CommonSerializer serializer = (CommonSerializer) CommonSerializer.getByCode(serializerCode);
        // 反序列化对象
        Object obj = null;
        System.out.println(packageType);
        if (packageType == PackageType.REQUEST_PACK) {
            // 处理请求包
            obj = serializer.deserialize(data, RpcRequest.class);
        } else if (packageType == PackageType.RESPONSE_PACK) {
            // 处理响应包
            obj = serializer.deserialize(data, RpcResponse.class);
        }

        // 将反序列化后的对象添加到输出列表
        if (obj != null) {
            list.add(obj);
        }
    }
}

