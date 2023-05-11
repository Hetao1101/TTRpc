package com.hetao.client;

import com.hetao.client.handler.RpcClientHandler;
import com.hetao.client.proxy.ServiceProxyHandler;
import com.hetao.service.RpcConfig;
import com.hetao.service.TestService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.I0Itec.zkclient.ZkClient;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RpcClient {


    private ZkClient zkClient;//zk客户端
    private RpcConfig config;//注册服务的配置类
    private List<String> registerServices;//管理注册的服务
    private Map<String,Object> proxyMap;//管理代理对象的容器
    private Map<String,List<ServiceInfo>> serviceInfoMap;//服务所对应的信息
    public RpcClient() {
        serviceInfoMap=new HashMap<>();
        proxyMap=new HashMap<>();
//        初始化zkClient
        initZkClient();

//        获取配置类的信息
        getServiceListConfig();
//        从配置类中找到需要被管理的服务
        registerServices= config.getServices();
//        获取zookeeper所有服务对应的信息
        getServiceInfosFromZk();
//        为注册的服务生成代理对象
        createProxyService();
//        把代理对象注册到spring容器中
        injectProxyToContainer();
    }

    private void injectProxyToContainer() {
    }

    private void createProxyService() {
        registerServices.forEach(service->{
            try {
                Class[] intefaces = null;
                Class<?> originService = Class.forName(service);
//                找到这个接口所有父接口
                List<Class> classList=new ArrayList<>();
                findParentService(originService,classList);

                intefaces= classList.stream().toArray(Class[]::new);
                Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), intefaces, new ServiceProxyHandler(service, this));
                TestService testService= (TestService) proxyInstance;
//            管理到容器当中
                proxyMap.put(service,proxyInstance);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        });
    }

    private void findParentService(Class<?> originService, List<Class> classList) {
        Class<?>[] interfaces = originService.getInterfaces();
        if(interfaces.length==0)//表示这个接口没有父接口了
            classList.add(originService);
        else
            Stream.of(interfaces).forEach(inter->findParentService(inter,classList));
    }

    private void initZkClient() {
        zkClient=new ZkClient("localhost:2181");
    }

    private void getServiceListConfig() {
        config=new RpcConfig();
    }

    private void getServiceInfosFromZk() {
        String root="/trpc";
        if(!zkClient.exists(root))
           throw new RuntimeException("没有服务可以提供");
        registerServices.forEach(registerService->{
            StringBuilder temPath=new StringBuilder(root).append("/").append(registerService);
            if(!zkClient.exists(temPath.toString()))
                throw new RuntimeException("没有服务可以提供");
            temPath.append("/providers");
            List<String> childrens = zkClient.getChildren(temPath.toString());
            if(childrens.size()==0)
                throw new RuntimeException(("没有服务可以提供"));
            childrens.forEach(serviceInfo->{
                String info = zkClient.readData(temPath.toString()+"/"+serviceInfo);
                String[] split = info.split(":");

                ServiceInfo serviceInfo1=new ServiceInfo();
                serviceInfo1.ip=split[0];
                serviceInfo1.port=split[1];
                List<ServiceInfo> serviceInfoList;
                if(!serviceInfoMap.containsKey(registerService))
                    serviceInfoList=new ArrayList<>();
                else
                    serviceInfoList=serviceInfoMap.get(registerService);
                serviceInfoList.add(serviceInfo1);
                serviceInfoMap.put(registerService,serviceInfoList);
            });
        });

    }


    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public RpcConfig getConfig() {
        return config;
    }

    public void setConfig(RpcConfig config) {
        this.config = config;
    }

    public List<String> getRegisterServices() {
        return registerServices;
    }

    public void setRegisterServices(List<String> registerServices) {
        this.registerServices = registerServices;
    }

    public Map<String, Object> getProxyMap() {
        return proxyMap;
    }

    public void setProxyMap(Map<String, Object> proxyMap) {
        this.proxyMap = proxyMap;
    }

    public Map<String, List<ServiceInfo>> getServiceInfoMap() {
        return serviceInfoMap;
    }

    public void setServiceInfoMap(Map<String, List<ServiceInfo>> serviceInfoMap) {
        this.serviceInfoMap = serviceInfoMap;
    }
}
