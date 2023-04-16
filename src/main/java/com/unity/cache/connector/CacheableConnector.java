package com.unity.cache.connector;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CacheableConnector<K extends Serializable> {
    /**
     * Get the value of the key from cache
     *
     * @param key the key
     * @return the value of the key
     */
    Optional<Object> getFromCache(K key);

    /**
     * Put the key-value pair into cache
     *
     * @param key   the key
     * @param value the value
     */
    void putToCache(K key, Object value);

    /**
     * Remove the key from cache
     *
     * @param key the key
     */
    void removeFromCache(K key);

    /**
     * Remove all keys from cache
     * It is implemented by the specific node cache implementation
     */
    void evictCache();

    /**
     * Get all entries from cache
     * It is implemented by the specific node cache implementation
     */
    Set<Map.Entry<K, Object>> getAllFromCache();
}
