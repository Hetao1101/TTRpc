package com.hetao.client.proxy;

import com.hetao.client.Invocation;
import com.hetao.client.RpcClient;
import com.hetao.client.ServiceInfo;
import com.hetao.client.codec.HessianCodec;
import com.hetao.client.codec.HessianDecoder;
import com.hetao.client.codec.HessianEncoder;
import com.hetao.client.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Random;
@Slf4j
public class ServiceProxyHandler implements InvocationHandler {
    private String serviceName;
    private RpcClient rpcClient;
    private Map<String, List<ServiceInfo>> serviceInfoMap;
    private RpcClientHandler rpcClientHandler;
    private Channel channel;//通道


    public ServiceProxyHandler() {

    }

    public ServiceProxyHandler(String serviceName, RpcClient rpcClient) {
        this.serviceName = serviceName;
        this.rpcClient = rpcClient;
        this.serviceInfoMap=rpcClient.getServiceInfoMap();
//        获取serviceInfo
//        建立与服务端的连接

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getDeclaringClass().equals(Object.class))
            return method.invoke(args);
        ServiceInfo serviceInfo=getServiceInfo();
        Object result = null;
        startClient(serviceInfo);

        Invocation invocation=new Invocation();
        invocation.setServiceName(serviceName);
        invocation.setMethodName(method.getName());
        invocation.setArgs(args);
        System.out.println("发送消息");
        writeMsg(invocation);
        
        return result;
    }

    private ServiceInfo getServiceInfo() {
        List<ServiceInfo> serviceInfoList = serviceInfoMap.get(serviceName);
        int index=new Random().nextInt(serviceInfoList.size());
        return serviceInfoList.get(index);
    }

    public void startClient(ServiceInfo serviceInfo)
    {
        String ip=serviceInfo.getIp();
        Integer port=Integer.valueOf(serviceInfo.getPort());
        NioEventLoopGroup eventExecutors=new NioEventLoopGroup();
        try{
//            创建bootstrap，配置参数
            Bootstrap bootstrap = new Bootstrap();
//            设置线程组
            bootstrap.group(eventExecutors).
                    channel(NioSocketChannel.class).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HessianEncoder());
                            socketChannel.pipeline().addLast(new HessianDecoder());
                            socketChannel.pipeline().addLast(rpcClientHandler=new RpcClientHandler());
                        }
                    });
            System.out.println("客户端已准备就绪。。。");
            ChannelFuture channelFuture=bootstrap.connect(ip,port).sync();
            channel=channelFuture.channel();
            System.out.println("已建立连接");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void writeMsg(Invocation invocation)
    {
        try{
            //        序列化对象
            channel.writeAndFlush(invocation);
            channel.close();
            System.out.println("连接已关闭");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
