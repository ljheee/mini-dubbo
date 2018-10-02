package com.ljheee.dubbo.util;

/**
 * Created by lijianhua04 on 2018/9/27.
 */
public class StringUtil {

    public static String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
