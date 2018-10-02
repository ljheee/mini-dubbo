package com.ljheee.dubbo.balance;

import java.util.List;

/**
 * 负载均衡 接口
 */
public interface LoadBalance {

    String select(List<String> lists);
}
