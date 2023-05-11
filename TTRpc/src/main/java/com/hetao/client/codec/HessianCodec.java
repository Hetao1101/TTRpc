package com.hetao.client.codec;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.Serializer;
import com.hetao.client.Invocation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class HessianCodec extends MessageToMessageCodec<ByteBuf, Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        Hessian2Output hessian2Output=new Hessian2Output(byteArrayOutputStream);
        try {
            System.out.println(msg);
            hessian2Output.startMessage();
            hessian2Output.writeObject(msg);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            byte[] result = byteArrayOutputStream.toByteArray();

            System.out.println("result="+result.length);
            buf.writeBytes(result);
            out.add(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null!=hessian2Output)
                {
                    hessian2Output.close();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        System.out.println("执行反序列化方法");
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        ByteArrayInputStream byteInputStream=new ByteArrayInputStream(bytes);
        Hessian2Input   hessian2Input=new Hessian2Input(byteInputStream);
        Invocation object=null;
        try {
            hessian2Input.startMessage();
            object=(Invocation) hessian2Input.readObject();
            hessian2Input.completeMessage();
            System.out.println(object.getMethodName());
            out.add(object);
            System.out.println(object);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null !=hessian2Input){
                    hessian2Input.close();
                    byteInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
