package com.ljheee.dubbo.registry;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {
    String discovery(String serviceName);
}
