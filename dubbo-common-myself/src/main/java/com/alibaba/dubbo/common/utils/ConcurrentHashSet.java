package com.alibaba.dubbo.common.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>,java.io.Serializable {
    
   private static final Object PRESENT = new Object();
    
   private final ConcurrentHashMap<E,Object> map;
    
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<E,Object>();
    }
    
    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<E,Object>(initialCapacity);
    }
    
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public boolean Contains(Object o) {
        return map.containsKey(o);
    }
    
    public boolean add(E e) {
        //map.put(key,value) if key not exists, return null,otherwise return oldvalue.
        return map.put(e, PRESENT) == null;
    }
    
    public boolean remove(Object o) {
        //map.remove(key) return result: the previous value associated with key,or null if there was no mapping for key.
        return map.remove(o) == PRESENT;
    }
    
    public void clear() {
        map.clear();
    }
    
}
