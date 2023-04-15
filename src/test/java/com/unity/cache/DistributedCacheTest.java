package com.unity.cache;

import com.unity.cache.node.Node;
import com.unity.cache.node.NodeManager;
import com.unity.cache.node.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DistributedCacheTest extends AbstractTest {

    private final static Map<Serializable, Object> DATA = new HashMap<>();

    private final NodeManager nodeManager = NodeManager.getInstance();
    private DistributedCache distributedCache;

    @BeforeEach
    void setUp() {

        DATA.clear();
        DATA.put(1, createObject(String.class));
        DATA.put(createObject(String.class), 2);
        DATA.put(createObject(Integer.class), createObject(String.class));
        DATA.put(createObject(TestKey.class), createObject(TestValue.class));

        Node node1 = new Node("node1", 123, NodeType.REDIS);
        Node node2 = new Node("node2", 123, NodeType.MEMCACHE);
        Node node3 = new Node("node3", 123, NodeType.REDIS);
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(node3);
        nodeManager.init(nodeList, 3);
        distributedCache = new DistributedCache(nodeManager);
    }

    @Test
    void testCache_add_get_givenFixedNodeNumber_allPass() {
        //Put data to 3-node 3-replica cache
        putEntryToCache();
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isEqualTo(value);
        });
    }

    @Test
    void testCache_delete_givenFixedNodeNumber_allPass() {
        //Put data to 3-node 3-replica cache then delete one entry
        putEntryToCache();
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isEqualTo(value);
        });
        distributedCache.remove(1);
        assertThat(distributedCache.get(1)).isNull();
        int totalSize = getTotalCacheContentAmount(3);
        assertThat(totalSize).isEqualTo(3);
    }

    @Test
    void testCache_givenAddNewNode_allPass() {
        //Put data to 3-node 3-replica cache then add one node
        putEntryToCache();
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4);

        nodeManager.nodeAdded(new Node("node4", 123, NodeType.REDIS));
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isEqualTo(value);
        });

        //All cache content is shuffled to four nodes but total should same
        totalCacheContentAmount = getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(4);
    }

    @Test
    void testCache_givenAddNewNode_bigCache_allPass() {
        //Put data to 3-node 3-replica cache then add one node, put 100000 keys in
        int AMOUNT = 100000;
        for (int i = 0; i < AMOUNT; i++) {
            TestKey key = new TestKey(i);
            TestValue value = new TestValue("value" + i);
            distributedCache.put(key, value);
        }
        for (int j = 0; j < AMOUNT; j++) {
            TestKey key = new TestKey(j);
            TestValue value = new TestValue("value" + j);
            assertThat(((TestValue) distributedCache.get(key)).getValue()).isEqualTo(value.getValue());
        }
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(AMOUNT);

        nodeManager.nodeAdded(new Node("node4", 123, NodeType.REDIS));
        for (int j = 0; j < AMOUNT; j++) {
            TestKey key = new TestKey(j);
            TestValue value = new TestValue("value" + j);
            assertThat(((TestValue) distributedCache.get(key)).getValue()).isEqualTo(value.getValue());
        }

        //All cache content is shuffled to four nodes but total should same
        totalCacheContentAmount = getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(AMOUNT);
    }

    @Test
    void testCache_givenShutdownNode_allPass() {
        //Put data to 3-node 3-replica cache then shutdown one node
        Node node4 = new Node("node4", 123, NodeType.REDIS);
        nodeManager.nodeAdded(node4);
        putEntryToCache();
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount =getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(4);

        //Node is down, all entries should be moved to other nodes. Total size should be same
        nodeManager.nodeShuttingDown(node4);
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isEqualTo(value);
        });
        //Total amount of entries are distributed to three nodes should be same
        totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4);
    }

    @Test
    void testCache_givenRemoveNode_allPass() {
        //Put data to 3-node 3-replica cache then remove one node
        Node node4 = new Node("node4", 123, NodeType.REDIS);
        nodeManager.nodeAdded(node4);
        putEntryToCache();
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(4);

        assertThat(totalCacheContentAmount).isEqualTo(4);

        //Node is down by failure, cache content in this node is lost.
        //Remove a node with content in its cache, left cache content shoubd be equal 4 - contentSize in this node
        Node nodeToRemove = nodeManager.getHashedNodeList().stream()
                .filter(node -> node.getCache().getAllEntries().size() > 1).findFirst().get();
        nodeManager.nodeRemoved(nodeToRemove);
        //Total amount of entries are distributed to three nodes should be same
        totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4 - nodeToRemove.getCache().getAllEntries().size());
    }

    private void putEntryToCache() {
        DATA.forEach((key, value) -> distributedCache.put(key, value));
    }

    private int getTotalCacheContentAmount(int size) {
        int total = 0;
        for (int i = 0; i < size; i++) {
            total += nodeManager.getHashedNodeList().get(i).getCache().getAllEntries().size();
        }
        return total;
    }
}

@Data
@AllArgsConstructor
class TestKey implements Serializable {
    private int key;
}

@Data
@AllArgsConstructor
class TestValue {
    private String value;
}