package com.ljheee.client;

import com.ljheee.dubbo.registry.ServiceDiscovery;
import com.ljheee.dubbo.registry.impl.ServiceDiscoveryImpl;
import com.ljheee.dubbo.rpc.RpcClientProxy;
import com.ljheee.server.service.HelloService;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class ClientMain {
    public static void main(String[] args) {

        ServiceDiscovery serviceDiscovery = new ServiceDiscoveryImpl();
        RpcClientProxy rpqClientProxy = new RpcClientProxy(serviceDiscovery);
        System.out.println(serviceDiscovery.discovery(HelloService.class.getName()));

        // 获取服务接口
        HelloService service = rpqClientProxy.create(HelloService.class);
        System.out.println(service);
        System.out.println(service.say("ljh , hahaha"));


    }
}
