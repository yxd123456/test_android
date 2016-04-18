package com.hz.util;

import java.util.HashMap;

/**
 * 多个key Map
 */
public class MultiKeyMap<K1, K2, V> {

    private HashMap<K1, HashMap<K2, V>> mapMap = new HashMap<>();


    /**
     * 添加数据
     **/
    public void put(K1 k1, K2 k2, V v) {
        if (k1 == null || k2 == null || v == null) {
            throw new RuntimeException("k1,k2,v都不可以为null");
        }
        HashMap<K2, V> k2VHashMap = mapMap.get(k1);
        if (k2VHashMap == null) {
            k2VHashMap = new HashMap<>();
        }
        k2VHashMap.put(k2, v);
        mapMap.put(k1, k2VHashMap);
    }

    /**
     * 根据读个key 获取数据
     **/
    public V get(K1 k1, K2 k2) {
        if (k1 == null || k2 == null) {
            throw new RuntimeException("k1,k2都不可以为null");
        }

        HashMap<K2, V> k2VHashMap = mapMap.get(k1);
        if (k2VHashMap != null) {
            return k2VHashMap.get(k2);
        }
        return null;
    }


}
