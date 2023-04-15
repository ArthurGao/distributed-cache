package com.unity.cache.connector;

import com.unity.cache.Cacheable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DummyMemcacheCache is a dummy Redis cache implementation of Cacheable interface.
 */
public class DummyRedisConnector implements Cacheable<Serializable> {
    //Just dummy, will be replaced with real Redis
    private final Map<Serializable, Object> cache;

    public DummyRedisConnector() {
        this.cache = new HashMap<>();
    }

    @Override
    public Object get(Serializable key) {
        return cache.get(key);
    }

    @Override
    public Set<Map.Entry<Serializable, Object>> getAllEntries() {
        return cache.entrySet();
    }

    @Override
    public void put(Serializable key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void remove(Serializable key) {
        cache.remove(key);
    }

    @Override
    public void evict() {
        cache.clear();
    }
}
