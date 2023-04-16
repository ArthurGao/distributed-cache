package com.unity.cache.connector;

import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DummyMemcacheCache is a dummy Redis cache implementation of Cacheable interface.
 */
public class DummyConnector implements CacheableConnector<Serializable> {

    //Just dummy, will be replaced with real Redis redis
    private final Map<Serializable, Object> cache;

    public DummyConnector(){
        //In real Memcache, we need to connect to the Redis server
        this.cache = new HashMap<>();
    }

    public DummyConnector(String ipAddress, int port) {
         this.cache = new HashMap<>();
    }

    @Override
    public Optional<Object> getFromCache(Serializable key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Set<Map.Entry<Serializable, Object>> getAllFromCache() {
        return cache.entrySet();
    }

    @Override
    public void putToCache(Serializable key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void removeFromCache(Serializable key) {
        cache.remove(key);
    }

    @Override
    public void evictCache() {
        cache.clear();
    }
}
