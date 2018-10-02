package com.ljheee.dubbo.registry;

/**
 * 服务注册接口
 */
public interface ServiceRegistryCenter {
    // 就是把serviceName(服务名称) :com.ljheee.api.IGpService
    // serviceAddresss(URL):127.0.0.1:8080 进行绑定，保存在注册中心

    void register(String serviceName, String serviceAddress);
}
