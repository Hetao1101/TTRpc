package com.hetao.server.handler;

import com.hetao.client.Invocation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("-0----------------");
        //获取客户端发送过来的消息
//        ByteBuf byteBuf=(ByteBuf) msg;

        System.out.println("-0----------------");
        Invocation invocation= (Invocation) msg;
        System.out.println("-0----------------");
        System.out.println(invocation.getServiceName());
        System.out.println("-0----------------");
        System.out.println(invocation.getMethodName());
        System.out.println("-0----------------");

//        Channel channel = ctx.channel();
//        System.out.println("收到客户端" + ctx.channel().remoteAddress() + "发送的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        发送消息给客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer("外币巴伯？",CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        发生异常，关闭通道
        ctx.close();

    }
}
