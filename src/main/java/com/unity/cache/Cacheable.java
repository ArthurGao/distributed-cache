package com.unity.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface Cacheable<K extends Serializable> {
    Object get(K key);

    void put(K key, Object value);

    void remove(K key);

    void evict();

    Set<Map.Entry<Serializable, Object>> getAllEntries();
}
