package com.alibaba.dubbo.common;

import java.util.HashMap;
import java.util.Map;

public class HashMapTest {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<String,String>();
        String result = map.put("wangyu", "123");
        System.out.println(result);
        result = map.put("wangyu", "1234");
        System.out.println(result);
    }
}
