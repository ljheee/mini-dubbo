package com.ljheee.dubbo.registry.impl;

import com.ljheee.dubbo.balance.LoadBalance;
import com.ljheee.dubbo.balance.impl.RandomLoadBalance;
import com.ljheee.dubbo.config.ZkConfig;
import com.ljheee.dubbo.registry.ServiceDiscovery;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery {


    List<String> lists = new ArrayList<>();
    private CuratorFramework curatorFramework;

    public ServiceDiscoveryImpl() {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZkConfig.zkAddress)
                .sessionTimeoutMs(4000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        //客户端已经连接 zk的服务端了
        curatorFramework.start();
    }

    @Override
    public String discovery(String serviceName) {


        String path = ZkConfig.zkRegistryPath + "/" + serviceName;

        try {
            lists = curatorFramework.getChildren().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 动态感知 服务节点变化
        registryWatch(path);

        // 服务 负载均衡选择
        LoadBalance loadBalance = new RandomLoadBalance();
        return loadBalance.select(lists);
    }

    /**
     * 感知path 节点变动
     *
     * @param path
     */
    private void registryWatch(String path) {

        PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, path, true);
        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                // 如果节点变动，重新获取一次
                lists = curatorFramework.getChildren().forPath(path);
            }
        };

        childrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            childrenCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
