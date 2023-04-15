package com.unity.cache;

import com.unity.cache.node.Node;
import com.unity.cache.node.NodeManager;
import com.unity.cache.node.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link DistributedCache}
 * <p>
 * Test cases of {@link DistributedCache} are divided into 3 parts:
 * 1. Test cache add/get/shutdown/remove given fixed node number
 * 2. Test cache add/get/shutdown/remove given dynamic node number
 * 3. Test cache add/get/shutdown/remove given dynamic node number and dynamic replica number
 * <p>
 */
class DistributedCacheTest extends AbstractTest {

    private final static Map<Serializable, Serializable> DATA = new HashMap<>();

    private final NodeManager nodeManager = NodeManager.getInstance();
    private DistributedCache distributedCache;

    @BeforeEach
    void setUp() throws IOException {

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
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(distributedCache.get(key).get()).isEqualTo(value);
        });
    }

    @Test
    void testCache_delete_givenFixedNodeNumber_allPass() {
        //Put data to 3-node 3-replica cache then delete one entry
        putEntryToCache();
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(distributedCache.get(key).get()).isEqualTo(value);
        });
        distributedCache.remove(1);
        assertThat(distributedCache.get(1)).isNotPresent();
        int totalSize = getTotalCacheContentAmount(3);
        assertThat(totalSize).isEqualTo(3);
    }

    @Test
    void testCache_givenAddNewNode_allPass() throws IOException {
        //Put data to 3-node 3-replica cache then add one node
        putEntryToCache();
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4);

        nodeManager.nodeAdded(new Node("node4", 123, NodeType.REDIS));
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(distributedCache.get(key).get()).isEqualTo(value);
        });

        //All cache content is shuffled to four nodes but total should same
        totalCacheContentAmount = getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(4);
    }

    @Test
    void testCache_givenAddNewNode_bigCache_allPass() throws IOException {
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
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(((TestValue) distributedCache.get(key).get()).getValue()).isEqualTo(value.getValue());
        }
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(AMOUNT);

        nodeManager.nodeAdded(new Node("node4", 123, NodeType.REDIS));
        for (int j = 0; j < AMOUNT; j++) {
            TestKey key = new TestKey(j);
            TestValue value = new TestValue("value" + j);
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(((TestValue) distributedCache.get(key).get()).getValue()).isEqualTo(value.getValue());
        }

        //All cache content is shuffled to four nodes but total should same
        totalCacheContentAmount = getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(AMOUNT);
    }

    @Test
    void testCache_givenShutdownNode_allPass() throws InterruptedException, IOException {
        //Put data to 3-node 3-replica cache then shutdown one node
        Node node4 = new Node("node4", 123, NodeType.REDIS);
        nodeManager.nodeAdded(node4);
        putEntryToCache();
        //Total amount of entries are distributed to three nodes should be same
        int totalCacheContentAmount = getTotalCacheContentAmount(4);
        assertThat(totalCacheContentAmount).isEqualTo(4);

        //Node is down, all entries should be moved to other nodes. Total size should be same
        nodeManager.nodeShuttingDown(node4);
        Thread.sleep(1000);
        DATA.forEach((key, value) -> {
            assertThat(distributedCache.get(key)).isPresent();
            assertThat(distributedCache.get(key).get()).isEqualTo(value);
        });
        //Total amount of entries are distributed to three nodes should be same
        totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4);
    }

    @Test
    void testCache_givenRemoveNode_allPass() throws IOException {
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
                .filter(node -> node.getCache().getAllFromCache().size() > 1).findFirst().get();
        nodeManager.nodeRemoved(nodeToRemove);
        //Total amount of entries are distributed to three nodes should be same
        totalCacheContentAmount = getTotalCacheContentAmount(3);
        assertThat(totalCacheContentAmount).isEqualTo(4 - nodeToRemove.getCache().getAllFromCache().size());
    }

    @Test
    void testValidateKey_givenInvalidKey_throwException() {
        assertThrows(IllegalArgumentException.class, () -> distributedCache.put(null, "value"));
    }

    @Test
    void testValidateValue_givenInvalidValue_throwException() {
        assertThrows(IllegalArgumentException.class, () -> distributedCache.put(1, null));
    }

    @Test
    void testValidateValue_givenNotExistingKey_throwException() {
        assertThat(distributedCache.get(2L)).isNotPresent();
    }

    private void putEntryToCache() {
        DATA.forEach((key, value) -> distributedCache.put(key, value));
    }

    private int getTotalCacheContentAmount(int size) {
        int total = 0;
        for (int i = 0; i < size; i++) {
            total += nodeManager.getHashedNodeList().get(i).getCache().getAllFromCache().size();
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
class TestValue implements Serializable {
    private String value;
}