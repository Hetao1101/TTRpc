package com.hetao.service;

import com.hetao.client.config.RpcServiceConfig;

import java.util.ArrayList;
import java.util.List;

public class RpcConfig implements RpcServiceConfig {
    @Override
    public List<String> getServices() {
        List<String> res=new ArrayList<>();
        res.add("com.hetao.service.TestService");
        return res;
    }
}
