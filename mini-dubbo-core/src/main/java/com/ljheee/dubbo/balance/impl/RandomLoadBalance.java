package com.ljheee.dubbo.balance.impl;

import com.ljheee.dubbo.balance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> lists) {

        int size = lists.size();
        Random random = new Random();
        return lists.get(random.nextInt(size));
    }
}
