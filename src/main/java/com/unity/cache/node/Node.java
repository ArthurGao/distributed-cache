package com.unity.cache.node;

import com.unity.cache.Cacheable;
import com.unity.cache.connector.DummyMemcacheConnector;
import com.unity.cache.connector.DummyRedisConnector;
import com.unity.cache.utils.ConsistentHashUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *   Node is a class which represents a node in the cache cluster
 *   It contains a cache backend which can be replaced by other cache backend
 *   It also contains a hash value which is used to calculate the hash ring
 *   It also contains a node id which is used to calculate the hash value
 *   It also contains a node type which is used to determine which cache backend to use
 *   It also contains a hostname and port which is used to connect to the node
 *   It also contains a cache backend which is used to store the cache
 */
@Data
public class Node implements Comparable<Node> {

    //Node id which is used to hash value
    private UUID nodeId;
    private String hostname;
    private int port;
    private NodeType type;

    //Hash value which comes from NodeManager(a double value in range [0,1))
    private Double hash;

    //This is a dummy cache backend, it can be replaced by other cache backend
    private Cacheable<Serializable> cache;

    public Node(String hostname,int port,NodeType type){
        this.hostname = hostname;
        this.port = port;
        this.type = type;
        this.cache = (this.type == NodeType.MEMCACHE) ? new DummyMemcacheConnector() : new DummyRedisConnector();
        this.nodeId = UUID.randomUUID();
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.hash, other.hash);
    }

    /**
     * Clear cache of this node
     * @return All entries in the cache
     */
    public Set<Map.Entry<Serializable, Object>> clearCache() {
        Set<Map.Entry<Serializable, Object>> currentEntries = new HashSet<>(cache.getAllEntries());
        this.cache.evict();
        return currentEntries;
    }

    /**
     * Put a key-value pair to cache
     * @param key key of the pair
     * @param value value of the pair
     */
    public void putToCache(Serializable key, Object value) {
        this.cache.put(key, value);
    }

    /**
     * Hash the node id to a double value, it should be in range [0, 1)
     * @param numReplicas replicas number
     */
    public void hash(int numReplicas) {
        if (numReplicas <= 0) {
            setHash(ConsistentHashUtil.myHash(getNodeId()));
        } else {
            for (int replica = 0; replica < numReplicas; replica++) {
                setHash(ConsistentHashUtil.myHash(getNodeId() + "_" + replica));
            }
        }
    }
}