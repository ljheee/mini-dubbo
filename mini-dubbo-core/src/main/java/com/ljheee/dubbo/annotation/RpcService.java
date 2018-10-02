package com.ljheee.dubbo.annotation;

import java.lang.annotation.*;

/**
 * 服务提供者使用 该注解
 * 用于标识 服务对外提供(需要注册到 注册中心)
 *
 * value 即为serviceName
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    Class<?> value() ;
}
