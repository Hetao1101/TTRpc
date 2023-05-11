package com.hetao;

import com.hetao.server.RpcServer;
import com.hetao.service.TestService;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ServerTest {
    @Test
    public void startServer() throws UnknownHostException {
//        RpcServer rpcServer=new RpcServer(6666,"com.hetao.service.UserService","com.hetao.service");
//        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }
    @Test
    public void testFile()
    {
        File file=new File("G:\\Redis-x64-3.0.504");
        System.out.println(file.isDirectory());
    }
    @Test
    public void classTest()
    {
        System.out.println(String.class.toString());
    }
    @Test
    public void proxyTest()
    {
        System.out.println(TestService.class);
        Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{TestService.class}, (x, y, z) -> "hhh");
        TestService testService= (TestService) proxyInstance;
        System.out.println(testService.say("zzz"));
        System.out.println(testService);

    }
}
