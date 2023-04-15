package com.unity.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Cacheable<K extends Serializable> {
    /**
     * Get the value of the key from cache
     * @param key the key
     * @return  the value of the key
     */
    Optional<Object> get(K key);

    /**
     * Put the key-value pair into cache
     * @param key the key
     * @param value the value
     */
    void put(K key, Object value);

    /**
     * Remove the key from cache
     * @param key the key
     */
    void remove(K key);

    /**
     * Remove all keys from cache
     * It is implemented by the specific node cache implementation
     * It is not supported by distributed cache
     */
    void evict();

    /**
     * Get all entries from cache
     * It is implemented by the specific node cache implementation
     * It is not supported by distributed cache
     */
    Set<Map.Entry<Serializable, Object>> getAllEntries();
}
