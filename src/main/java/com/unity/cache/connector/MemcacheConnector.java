package com.unity.cache.connector;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DummyMemcacheCache is a dummy Memcache implementation of Cacheable interface.
 */
public class MemcacheConnector implements CacheableConnector<Serializable> {

    private MemcachedClient memcachedClient;

    public MemcacheConnector(String ipAddress, int port) throws IOException {
        memcachedClient = new MemcachedClient(new InetSocketAddress(ipAddress, port));
    }

    @Override
    public Optional<Object> getFromCache(Serializable key) {
        return Optional.ofNullable(memcachedClient.get(key.toString()));
    }

    @Override
    public Set<Map.Entry<Serializable, Object>> getAllFromCache() {
        return Collections.emptySet();
    }

    @Override
    public void putToCache(Serializable key, Object value) {
        memcachedClient.set(key.toString(), 0, value);
    }

    @Override
    public void removeFromCache(Serializable key) {
        memcachedClient.delete(key.toString());
    }

    @Override
    public void evictCache() {
        memcachedClient.flush();
    }
}
