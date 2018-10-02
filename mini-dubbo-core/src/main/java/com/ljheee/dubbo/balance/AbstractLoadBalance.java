package com.ljheee.dubbo.balance;

import java.util.List;

/**
 * Created by lijianhua04 on 2018/9/26.
 */
public abstract class AbstractLoadBalance implements LoadBalance {


    @Override
    public String select(List<String> lists) {

        if (lists == null || lists.size() == 0) {
            return null;
        }

        if (lists.size() == 1) {
            return lists.get(0);
        }
        return doSelect(lists);
    }

    protected abstract String doSelect(List<String> lists);
}
