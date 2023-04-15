package com.unity.cache.node;

import com.unity.cache.Cacheable;
import com.unity.cache.connector.DummyMemcacheConnector;
import com.unity.cache.connector.DummyRedisConnector;
import com.unity.cache.utils.ConsistentHashUtil;
import lombok.Data;

import java.util.UUID;

/**
 * Node is a class which represents a node in the cache cluster
 */
@Data
public class Node implements Comparable<Node> {

    //Node id which is hashed key to a double value
    private UUID nodeId;
    private String hostname;
    private int port;
    private NodeType type;

    //Hash value which comes from NodeManager, it is hashed by index of node list ATM just for easy
    private Double hash;
    //This is a dummy cache backend, it can be replaced by other cache backend
    private Cacheable cache;

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