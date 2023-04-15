package com.unity.cache.node;

import com.unity.cache.utils.ConsistentHashUtil;
import com.unity.cache.exceptions.InternalException;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NodeManager is used to manage the nodes in the cluster.
 */
public class NodeManager implements NodeEventHandler {

    private static NodeManager instance;

    @Getter
    private final List<Node> hashedNodeList = new ArrayList<>();
    private int numReplicas;

    private NodeManager() {
    }

    /**
     * NodeManager is a singleton class with this as only entry point
     * @return NodeManager instance
     */
    public static NodeManager getInstance() {
        if (instance == null) {
            instance = new NodeManager();
        }
        return instance;
    }

    /**
     * Initialize the node manager with a list of nodes and number of replicas in cluster
     * @param nodeList
     * @param numReplicas
     */
    public void init(List<Node> nodeList, int numReplicas) {
        this.hashedNodeList.clear();
        this.numReplicas = numReplicas;
        if (numReplicas < 0) {
            throw new IllegalArgumentException("Replica number must be equal or larger than 0");
        }
        nodeList.forEach(node -> rearrangeNodeList(node, true));
    }

    /**
     * Get the node from cluster. The node is determined by the hash value of the key
     * Will always return a node
     * @param key
     * @return Node
     */
    public Node nodeGet(Serializable key) {
        if (key == null) {
            throw new IllegalArgumentException("Key can not be empty");
        }
        if (CollectionUtils.isEmpty(this.hashedNodeList)) {
            throw new InternalException("No available node(s), please check the cluster status or initialize the node manager");
        }
        if(this.hashedNodeList.size()==1){
            return this.hashedNodeList.get(0);
        }

        double hash = ConsistentHashUtil.myHash(key);
        if (hash > this.hashedNodeList.get(this.hashedNodeList.size() - 1).getHash()) {
            return this.hashedNodeList.get(0);
        }
        int index = ConsistentHashUtil.binarySearch(this.hashedNodeList.stream().map(Node::getHash).collect(Collectors.toList()), hash);
        return this.hashedNodeList.get(index);
    }

    /**
     * Add a node to the cluster.
     * Will add it to a position base on consistent hash algorithm in the node list
     * Will dispatch the cache of the previous node and next node to the new node
     * No cached content will be lost from the cluster (but shuffle between different nodes)
     */
    @Override
    public void nodeAdded(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node can not be null");
        }
        if (this.hashedNodeList.stream().anyMatch(obj -> obj.getNodeId() == node.getNodeId())) {
            throw new IllegalArgumentException("Node already exists");
        }
        rearrangeNodeList(node, true);

        int indexOfNewNode = findIndexOfNode(node);
        if (indexOfNewNode == 0) {
            // If the new node is the first node, then the next node and the last node cache should be dispatched
            shuffleNode(this.hashedNodeList.get(indexOfNewNode + 1), this.hashedNodeList.get(this.hashedNodeList.size() - 1));
        } else if (indexOfNewNode == this.hashedNodeList.size() - 1) {
            // If the new node is the last node, then the previous node and the first node cache should be dispatched
            shuffleNode(this.hashedNodeList.get(0), this.hashedNodeList.get(indexOfNewNode - 1));
        } else {
            // If the new node is not the first node, then the previous node and next node cache should be dispatched
            shuffleNode(this.hashedNodeList.get(indexOfNewNode - 1), this.hashedNodeList.get(indexOfNewNode + 1));
        }
    }

    /**
     * A node is removed from cluster without being managed shutting down(by accident or other reasons)
     * Will remove the node from the node list
     * Will NOT dispatch the cache of the this removed node to the new node
     * Cached content in this node will be lost
     * @param node
     */
    @Override
    public void nodeRemoved(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node to be deleted can not be null");
        }
        int index = findIndexOfNode(node);
        if (index < 0) {
            throw new IllegalArgumentException("Node to be deleted not found");
        }
        if (this.hashedNodeList.size() == 1) {
            throw new IllegalArgumentException("Can not delete the last node");
        }
        this.hashedNodeList.removeIf(obj -> obj.getNodeId() == node.getNodeId());
    }

    /**
     * A node is shutting down on purpose
     * Will remove the node from the node list
     * Will dispatch the cache of the this removed node to the new node
     * Cached content in this node will NOT be lost (But shuffled to other nodes)
     * @param node
     */
    @Override
    public void nodeShuttingDown(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node to be shutdown can not be null");
        }
        int index = findIndexOfNode(node);
        if (index < 0) {
            throw new IllegalArgumentException("Node to be shutdown not found");
        }
        if (this.hashedNodeList.size() == 1) {
            throw new IllegalArgumentException("Can not shutdown the last node");
        }
        rearrangeNodeList(node, false);
        shuffleNode(node);
    }

    /**
     * Consistent hashed circle is refreshed/rearrange because of node addition or removal
     */
    private void rearrangeNodeList(Node node, boolean isToAdd) {
        node.hash(this.numReplicas);
        if (isToAdd) {
            this.hashedNodeList.add(node);
        }
        else{
            this.hashedNodeList.removeIf(obj -> obj.getNodeId() == node.getNodeId());
        }
        this.hashedNodeList.sort(Node::compareTo);
    }

    private int findIndexOfNode(Node node) {
        return IntStream.range(0, this.hashedNodeList.size())
                .filter(i -> this.hashedNodeList.get(i).getNodeId() == node.getNodeId())
                .findFirst()
                .orElse(-1);
    }

    /**
     * Shuffle the cache of the node to other nodes
     *  Step 1: Get all the cache entries from the node(s) to be shuffled
     *  Step 2: Evict all the cache entries from the node(s) to be shuffled
     *  Step 3: Dispatch the cache entries to the cluster
     * @param nodeList
     */
    private void shuffleNode(Node... nodeList) {
        Arrays.stream(nodeList).forEach(node -> {
            Set<Map.Entry<Serializable, Object>> currentEntries = new HashSet<>(node.getCache().getAllEntries());
            node.getCache().evict();
            currentEntries.forEach(entry -> {
                Node nodeCandidate = nodeGet(entry.getKey());
                nodeCandidate.getCache().put(entry.getKey(), entry.getValue());
            });
        });
    }
}

