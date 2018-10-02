package com.ljheee.server.service.impl;

import com.ljheee.dubbo.annotation.RpcService;
import com.ljheee.server.service.HelloService;

/**
 * 服务实现
 * @RpcService 注解指定接口
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String name) {
        return "6666" + name;
    }
}
