package com.hetao;

import com.hetao.server.RpcServer;

public class TestServer {
    public static void main(String[] args) {
        RpcServer rpcServer=new RpcServer(6666,"com.hetao.service");
    }

}
