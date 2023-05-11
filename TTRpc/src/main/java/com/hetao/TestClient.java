package com.hetao;

import com.hetao.client.RpcClient;
import com.hetao.service.TestService;
import com.hetao.service.impl.TestServiceImpl;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;

public class TestClient {
    TestServiceImpl testService=new TestServiceImpl();
    TestServiceImpl testService2=new TestServiceImpl();
    int a=2;
    private final static int anInt=1;
    public static void main(String[] args) {
        RpcClient rpcClient=new RpcClient();
        TestService testService1= (TestService) rpcClient.getProxyMap().get("com.hetao.service.TestService");
        testService1.say("bbb");
        testService1.say("ccc");
    }

}
