package com.unity.cache.node;

import com.unity.cache.AbstractTest;
import com.unity.cache.exceptions.InternalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class NodeManagerTest extends AbstractTest {

    @Test
    void testGetNode_given_3nodes_0Replica_happyCase_noException() {
        //3 nodes + 0 replicas happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        assertKeyDispatched(nodeList, 3);
    }

    @Test
    void testGetNode_given_3nodes_0Replica_bigAmountKeys_dispatchEvenly() {
        //3 nodes + 0 replicas put 100000 keys
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        //100000 key-value should be dispatched to node evenly
        assertKeyDispatched(nodeList, 100000);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void testGetNode_given_3nodes_multipleReplica_happyCase_sameWith0Replica(int replicaNum) {
        //Given 3 nodes + 1,3,5 replica, happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, replicaNum);
        //Should be same with 0 replica
        assertKeyDispatched(nodeList, 3);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void testGetNode_given_3nodes_multipleReplica_bigAmountKeys_dispatchEvenly(int replicaNum) {
        //Given 3 nodes + 1,3,5 replica, put 10000 keys
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, replicaNum);
        //Should be same with 0 replica
        assertKeyDispatched(nodeList, 10000);
    }

    @Test
    void testGetNode_given_nullKey_getIllegalArgumentException() {
        //Get node with null key, get IllegalArgumentException
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        assertThrows(IllegalArgumentException.class, () ->
                nodeManager.nodeGet(null));
    }

    @Test
    void testGetNode_given_0node_getIllegalArgumentException() {
        //Get node with 0 node, get InternalException
        List<Node> nodeList = getNodes(0);
        nodeManager.init(nodeList, 0);
        assertThrows(InternalException.class, () ->
                nodeManager.nodeGet("1"));
    }

    @Test
    void testGetNode_given_negativeReplica_getIllegalArgumentException() {
        //Given negative replicas number, get IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                nodeManager.init(getNodes(1), -1));
    }

    @Test
    void testNodeAdd_given_addOneNode_0Replica_happyCase_noException() {
        //Given 3 nodes + 0 replicas, add one node, happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        Node newNode = createObject(Node.class);
        nodeManager.nodeAdded(newNode);
        nodeList.add(newNode);
        assertKeyDispatched(nodeList, 4);
    }

    @Test
    void testNodeAdd_given_addOneNode_0Replica_bigAmountKeys_dispatchEvenly() {
        //Given 3 nodes + 0 replicas, add one node, put 100000 keys
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        Node newNode = createObject(Node.class);
        nodeManager.nodeAdded(newNode);
        nodeList.add(newNode);
        assertKeyDispatched(nodeList, 100000);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void testNodeAdd_given_addOneNode_multipleReplica_bigAmountKeys_dispatchEvenly(int replicaNum) {
        //Given 3 nodes + 1,3,5 replicas, add one node, happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, replicaNum);
        Node newNode = createObject(Node.class);
        nodeManager.nodeAdded(newNode);
        nodeList.add(newNode);
        assertKeyDispatched(nodeList, 10000);
    }

    @Test
    void testGetNode_given_nullNode_getIllegalArgumentException() {
        //Add null node, get IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                nodeManager.nodeAdded(null));
    }

    @Test
    void testGetNode_given_addExistingNodeId_getIllegalArgumentException() {
        //Add existing node, get IllegalArgumentException
        List<Node> nodeList = getNodes(1);
        nodeManager.init(nodeList, 0);
        assertThrows(IllegalArgumentException.class, () -> nodeManager.nodeAdded(nodeList.get(0)));
    }

    @Test
    void testNodeDelete_given_deleteOneNode_0Replica_happyCase_noException() {
        //Given 3 nodes + 0 replicas, put a node, happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        nodeManager.nodeRemoved(nodeList.get(0));
        nodeList.remove(0);
        assertKeyDispatched(nodeList, 2);
    }

    @Test
    void testNodeDelete_given_deleteOneNode_0Replica_bigAmountKeys_noException() {
        //Given 3 nodes + 0 replicas, put a node, put 100000 keys
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, 0);
        nodeManager.nodeRemoved(nodeList.get(0));
        nodeList.remove(0);
        assertKeyDispatched(nodeList, 100000);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void testNodeDelete_given_deleteOneNode_multipleReplica_bigAmountKeys_noException(int replicaNum) {
        //Given 3 nodes + 1,3,5 replicas, put a node, happy case
        List<Node> nodeList = getNodes(3);
        nodeManager.init(nodeList, replicaNum);
        nodeManager.nodeRemoved(nodeList.get(0));
        nodeList.remove(0);
        assertKeyDispatched(nodeList, 100000);
    }

    @Test
    void testGetNode_given_deleteNotExistingNode_getIllegalArgumentException() {
        //Delete not existing node, get IllegalArgumentException
        List<Node> nodeList = getNodes(1);
        nodeManager.init(nodeList, 0);
        Node node = createObject(Node.class);
        assertThrows(IllegalArgumentException.class, () -> nodeManager.nodeRemoved(node));
    }

    @Test
    void testGetNode_given_deleteNullNode_getIllegalArgumentException() {
        //Delete null node, get IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> nodeManager.nodeRemoved(null));
    }

    @Test
    void testGetNode_given_deleteTheLastNode_getIllegalArgumentException() {
        //Delete the last node, get IllegalArgumentException
        List<Node> nodeList = getNodes(1);
        nodeManager.init(nodeList, 0);
        assertThrows(IllegalArgumentException.class, () -> nodeManager.nodeRemoved(nodeList.get(0)));
    }
}