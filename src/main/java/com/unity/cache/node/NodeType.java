package com.unity.cache.node;

public enum NodeType {
    REDIS, MEMCACHE;

    public static NodeType fromString(String type) {
        if (type.equalsIgnoreCase("redis")) {
            return REDIS;
        } else if (type.equalsIgnoreCase("memcache")) {
            return MEMCACHE;
        } else {
            throw new IllegalArgumentException("Invalid node type: " + type);
        }
    }
}
