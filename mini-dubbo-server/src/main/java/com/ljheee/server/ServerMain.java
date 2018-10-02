package com.ljheee.server;

import com.ljheee.dubbo.registry.ServiceRegistryCenter;
import com.ljheee.dubbo.registry.impl.ZkServiceRegistryCenterImpl;
import com.ljheee.dubbo.rpc.RpcServer;
import com.ljheee.server.service.HelloService;
import com.ljheee.server.service.impl.HelloServiceImpl;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class ServerMain {
    public static void main(String[] args) {

        HelloService service = new HelloServiceImpl();
        ServiceRegistryCenter registryCenter = new ZkServiceRegistryCenterImpl();
        String serviceAddress = "127.0.0.1:8080";
//        registryCenter.register("helloService",serviceAddress);
//        registryCenter.register("helloService","127.0.0.2:9090");
//        while(true){}

        RpcServer rpcServer = new RpcServer(registryCenter, serviceAddress);
        rpcServer.bind(service);
        rpcServer.registerAndListen();



    }
}
