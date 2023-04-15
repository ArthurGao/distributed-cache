package com.unity.cache;

import com.unity.cache.node.NodeManager;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *   DistributedCache is a distributed caching mechanism using a consistent hashing algorithm.
 *   It uses a NodeManager to get the node where the key is stored.
 *
 *   e.g.
 *          NodeManager nodeManager = new NodeManager();
 *          nodeManager.addNode(new Node("node1"...));
 *          nodeManager.addNode(new Node("node2"...));
 *          nodeManager.init();
 *          DistributedCache distributedCache = new DistributedCache(nodeManager);
 *          distributedCache.put("key1", "value1");
 */
public class DistributedCache<K extends Serializable> implements Cacheable<K> {

    private final NodeManager nodeManager;

    public DistributedCache(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    public Object get(K key) {
        return nodeManager.nodeGet(key).getCache().get(key);
    }

    @Override
    public void put(K key, Object value) {
        nodeManager.nodeGet(key).getCache().put(key, value);
    }

    @Override
    public void remove(K key) {
        nodeManager.nodeGet(key).getCache().remove(key);
    }

    @Override
    public void evict() {
        throw new UnsupportedOperationException("Can't evict all entries from distributed cache for all nodes.");
    }

    @Override
    public Set<Map.Entry<Serializable, Object>> getAllEntries() {
        throw new UnsupportedOperationException("Can't get all entries from distributed cache for all nodes.");
    }
}
