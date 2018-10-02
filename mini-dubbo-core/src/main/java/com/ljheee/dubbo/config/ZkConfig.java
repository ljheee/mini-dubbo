package com.ljheee.dubbo.config;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class ZkConfig {
    //zk集群的地址,客户端连接的地址
    public final static String zkAddress = "127.0.0.1:2181";

    //所有服务发布在这个节点 下面
    public final static String zkRegistryPath = "/registrys";
}
