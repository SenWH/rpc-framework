package org.example.CustomUtil.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.Common.AgrType;
import org.example.Common.PackageType;
import org.example.CustomUtil.CommonSerializer;
import org.example.POJO.RpcRequest;

// 自定义编码器，序列化对象并添加长度字段
public class CustomObjectEncoder extends MessageToByteEncoder<Object> {

    private final CommonSerializer serializer;

    public CustomObjectEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeInt(AgrType.RPC_CUSTOM);
        byte[] bytes = serializer.serialize(msg);
        if(msg instanceof RpcRequest) {  //包类型
            out.writeInt(PackageType.REQUEST_PACK);
        } else {
            out.writeInt(PackageType.RESPONSE_PACK);
        }

        out.writeInt(serializer.getCode());
        // 写入长度字段 防止粘包
        out.writeInt(bytes.length);
        // 写入对象数据
        out.writeBytes(bytes);
    }
}
