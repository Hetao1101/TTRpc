package com.hetao.server;

import com.hetao.client.codec.HessianCodec;
import com.hetao.client.codec.HessianDecoder;
import com.hetao.client.codec.HessianEncoder;
import com.hetao.server.annotation.TRpcService;
import com.hetao.server.handler.RpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class RpcServer {
    private ZkClient zkClient;
    private Integer port;
    private String separator= File.separator;
    private String rootPath;
    private List<String> rpcServiceClassList;
    private List<String> registerServiceNameList;
    private List<String> serverClassList;
    private String basePackage;
    private Map<String,Object> serviceBeans;
    public RpcServer(Integer port,String basePackage)
    {
        this.basePackage=basePackage;
        this.port=port;
        registerServiceNameList=new ArrayList<>();
        rpcServiceClassList=new ArrayList<>();
        serverClassList=new ArrayList<>();
        serviceBeans=new HashMap<>();
        rootPath=this.getClass().getClassLoader().getResource("").getPath();
        //加载并管理所有的bean对象
        loadAllService();
        //启动服务器监听客户端消息
        new Thread(()->startServer()).start();

        //向zookeeper中心注册该服务器
        registerToZk();
    }

    private void loadAllService() {
        String basePath=basePackage.replace(".",separator);
        //todo 查找该路径下的所有class文件
        findServiceClass(rootPath+basePath+separator);
        //todo 找到有rpcService注解的class文件
        findRpcServiceClass();

        //todo 从spring容器中获取所有的service对象并管理
        getServiceBeans();
        log.info("已初始化服务器完毕");
    }

    private void getServiceBeans() {
        //todo 假设从spring容器中获取对应的bean对象
        getBeanFromSpring();

    }

    private void getBeanFromSpring() {
        rpcServiceClassList.forEach(rpcServiceName->{
            try {
                serviceBeans.put(rpcServiceName,Class.forName(rpcServiceName).newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void findRpcServiceClass() {
        serverClassList.forEach(className->{
            try {
                Class temClass=Class.forName(className);
                if(temClass.isAnnotationPresent(TRpcService.class))
                {
                    TRpcService annotation = (TRpcService) temClass.getAnnotation(TRpcService.class);

                    rpcServiceClassList.add(className);
                    registerServiceNameList.add(annotation.value());

                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void findServiceClass(String root) {
         File file=new File(root);
         if(file.isFile())
         {
             String name = file.getName();
             String extName=name.substring(name.lastIndexOf(".")+1);
             if("class".equals(extName))
             {
                 String tem=new String(root);
                 tem=tem.substring(tem.indexOf("classes")+8);
                 if(separator.equals("\\"))
                    tem=tem.replaceAll("\\\\",".");
                 else tem=tem.replaceAll("/",".");
                 tem=tem.substring(0,tem.lastIndexOf("."));
                 serverClassList.add(tem);

             }
             return;
         }
         File[] files = file.listFiles();

        Stream.of(files).forEach(f -> findServiceClass(f.getPath()));


    }


    private void registerToZk() {
        zkClient=new ZkClient("127.0.0.1:2181");
        log.info("已和注册中心进行连接。。。");
//        创建持久化节点
        createPoints();
    }

    private void createPoints() {
        try {
            String root="/trpc";
            //判断trpc节点存不存在
            if(!zkClient.exists(root))//不存在则创建这个持久化节点
                zkClient.createPersistent(root,"hetao");
            //创建服务节点
            registerServiceNameList.forEach(serverName->{
                String serverPath=root+"/"+serverName;
                if(!zkClient.exists(serverPath))
                    zkClient.createPersistent(serverPath,serverName);
                //创建提供方节点
                String providersPath=serverPath+"/"+"providers";
                if(!zkClient.exists(providersPath))
                    zkClient.createPersistent(providersPath,"providers");
                //获取本机ip地址，用于创建临时节点
                String serverAddress= null;
                try {
                    serverAddress = InetAddress.getLocalHost().getHostAddress() +":"+port;
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                String serverAddressPath=providersPath+"/"+serverAddress;
                if(zkClient.exists(serverAddressPath))
                    zkClient.delete(serverAddressPath);
                zkClient.createEphemeral(serverAddressPath,serverAddress);
            });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public  void startServer() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
//            创建服务端启动对象,设置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            设置两个线程组bossGroup和workerGroup
            serverBootstrap.group(bossGroup,workerGroup)
//                    设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
//                    设置线程队列得到的连接个数
                    .option(ChannelOption.SO_BACKLOG,128)
//                    设置保持活动的连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
//                   匿名内部类方式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            socketChannel.pipeline().addLast(new HessianEncoder());
                            socketChannel.pipeline().addLast(new HessianDecoder());
                            socketChannel.pipeline().addLast(new RpcServerHandler());
                        }
                    });
            System.out.println("服务端已初始化完毕。。。");
//            绑定端口号启动服务端
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();

//            都关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
