package com.ljheee.dubbo.registry.impl;

import com.ljheee.dubbo.config.ZkConfig;
import com.ljheee.dubbo.registry.ServiceRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class ZkServiceRegistryCenterImpl implements ServiceRegistryCenter {

    //zk写入节点   zkClient Curator 实现对zk的操作
    private CuratorFramework curatorFramework;

    public ZkServiceRegistryCenterImpl() {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZkConfig.zkAddress)
                .sessionTimeoutMs(4000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        //客户端已经连接 zk的服务端了
        curatorFramework.start();

    }


    //服务注册
    @Override
    public void register(String serviceName, String serviceAddress) {

        //构建  /registrys/com.xxx.ServiceName  持久化节点
        String servicePath = ZkConfig.zkRegistryPath + "/" + serviceName;
        try {
            if (curatorFramework.checkExists().forPath(servicePath) == null) {
                curatorFramework.create().creatingParentsIfNeeded().
                        withMode(CreateMode.PERSISTENT).forPath(servicePath, "0".getBytes());
            }
            System.out.println("serviceName 构建成功:" + servicePath);

            //registrys/com.xxx.ServiceName/127.0.0.1:8080   临时节点
            String addressPath = servicePath + "/" + serviceAddress;
            String addNode = curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(addressPath, "0".getBytes());
            System.out.println("serviceAddress 临时节点构建成功 :" + addNode);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
