package com.hetao;

import com.hetao.server.RpcServer;

public class TestServer2 {
    public static void main(String[] args) {
        RpcServer rpcServer=new RpcServer(6667,"com.hetao.service");
    }

}
