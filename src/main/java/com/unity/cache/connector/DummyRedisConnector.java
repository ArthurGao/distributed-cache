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
public class DummyRedisConnector implements CacheableConnector<Serializable, Serializable> {

    //Just dummy, will be replaced with real Redis redis
    private final Map<Serializable, Serializable> cache;
    private Jedis jedis;

    public DummyRedisConnector() {
        this.cache = new HashMap<>();
    }

    public DummyRedisConnector(String ipAddress, int port) {
        this.cache = new HashMap<>();
        //In real Memcache, we need to connect to the Redis server
        //jedis = new Jedis(ipAddress, port);
    }

    @Override
    public Optional<Serializable> getFromCache(Serializable key) {
        //In real Redis, we need to get the value from the Redis server
        //return Optional.ofNullable(jedis.get(key));
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Set<Map.Entry<Serializable, Serializable>> getAllFromCache() {
        //In real Redis, we need to get all entries from the Redis server
        //return jedis.getStats("items").keySet();
        return cache.entrySet();
    }

    @Override
    public void putToCache(Serializable key, Serializable value) {
        //In real Redis, we need to put the key-value pair into the Redis server
        //jedis.set(key, value);
        cache.put(key, value);
    }

    @Override
    public void removeFromCache(Serializable key) {
        //In real Redis, we need to remove the key from the Redis server
        //jedis.del(key);
        cache.remove(key);
    }

    @Override
    public void evictCache() {
        //In real Redis, we need to remove all keys from the Redis server
        //jedis.flushAll();
        cache.clear();
    }
}
