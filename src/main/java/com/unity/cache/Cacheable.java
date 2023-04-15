package com.unity.cache;

import java.io.Serializable;
import java.util.Optional;

public interface Cacheable {
    /**
     * Get the value of the key from cache
     *
     * @param key the key
     * @return the value of the key
     */
    Optional<Serializable> get(Serializable key);

    /**
     * Put the key-value pair into cache
     *
     * @param key   the key
     * @param value the value
     */
    void put(Serializable key, Serializable value);

    /**
     * Remove the key from cache
     *
     * @param key the key
     */
    void remove(Serializable key);
}
