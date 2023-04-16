package com.unity.cache.connector;

import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.*;

/**
 * DummyMemcacheCache is a dummy Redis cache implementation of Cacheable interface.
 */
public class RedisConnector implements CacheableConnector<Serializable> {

    private Jedis jedis;

    public RedisConnector(String ipAddress, int port) {
        jedis = new Jedis(ipAddress, port);
    }

    @Override
    public Optional<Object> getFromCache(Serializable key) {
        return Optional.ofNullable(jedis.get(key.toString()));
    }

    @Override
    public Set<Map.Entry<Serializable, Object>> getAllFromCache() {
        Set<Map.Entry<String, String>> stringSet = jedis.hgetAll("*").entrySet();
        Set<Map.Entry<Serializable, Object>> objectSet = new HashSet<>();
        for (Map.Entry<String, String> entry : stringSet) {
            Map.Entry<Serializable, Object> newEntry = new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            objectSet.add(newEntry);
        }
        return objectSet;
    }

    @Override
    public void putToCache(Serializable key, Object value) {
        jedis.set(key.toString(), value.toString());
    }

    @Override
    public void removeFromCache(Serializable key) {
        jedis.del(key.toString());
    }

    @Override
    public void evictCache() {
        jedis.flushAll();
    }
}
