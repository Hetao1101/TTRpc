package com.hetao.service.impl;

import com.hetao.server.annotation.TRpcService;
import com.hetao.service.TestService;
@TRpcService("com.hetao.service.TestService")
public class TestServiceImpl implements TestService {
    @Override
    public String say(String hello)
    {
        return "服务器端说："+hello;
    }
}
