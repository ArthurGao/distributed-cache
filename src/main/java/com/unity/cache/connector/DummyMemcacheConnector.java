package com.unity.cache.connector;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DummyMemcacheCache is a dummy Memcache implementation of Cacheable interface.
 */
public class DummyMemcacheConnector implements CacheableConnector<Serializable, Serializable> {

    //Just dummy, will be replaced with real Memcache memcachedClient
    private final Map<Serializable, Serializable> cache;
    private MemcachedClient memcachedClient;

    public DummyMemcacheConnector(String ipAddress, int port) throws IOException {
        //Just dummy, will be replaced with real Memcache memcachedClient
        this.cache = new HashMap<>();
        //In real Memcache, we need to connect to the Memcache server
        //memcachedClient = new MemcachedClient(new InetSocketAddress(ipAddress, port));
    }

    @Override
    public Optional<Serializable> getFromCache(Serializable key) {
        //In real Memcache, we need to get the value from the Memcache server
        //return Optional.ofNullable(memcachedClient.get(key));
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Set<Map.Entry<Serializable, Serializable>> getAllFromCache() {
        //In real Memcache, we need to get all entries from the Memcache server
        //return memcachedClient.getStats("items").keySet();
        return cache.entrySet();
    }

    @Override
    public void putToCache(Serializable key, Serializable value) {
        //In real Memcache, we need to put the key-value pair into the Memcache server
        //memcachedClient.set(key, 0, value);
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
