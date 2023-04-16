package com.unity.cache;

import com.unity.cache.connector.CacheableConnector;
import com.unity.cache.connector.DummyConnector;
import com.unity.cache.node.Node;
import com.unity.cache.node.NodeManager;
import com.unity.cache.utils.ConsistentHashUtil;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTest {
    protected final NodeManager nodeManager = NodeManager.getInstance();
    private EasyRandom generator;

    /**
     * Find the node with max hash value by binary search
     */
    private static Node findMaxHashNode(List<Node> nodeList) {
        Node maxHashNode = nodeList.get(0);
        for (int i = 1; i < nodeList.size(); i++) {
            if (nodeList.get(i).getHash() > maxHashNode.getHash()) {
                maxHashNode = nodeList.get(i);
            }
        }
        return maxHashNode;
    }

    /**
     * Find the node with min hash value by binary search
     */
    private static Node findMinHashNode(List<Node> nodeList) {
        Node minHashNode = nodeList.get(0);
        for (int i = 1; i < nodeList.size(); i++) {
            if (nodeList.get(i).getHash() <= minHashNode.getHash()) {
                minHashNode = nodeList.get(i);
            }
        }
        return minHashNode;
    }

    /**
     * Create a random object for test
     */
    protected <T> T createObject(EasyRandom generator, Class<T> c) {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .randomize(CacheableConnector.class, DummyConnector::new);
        generator = new EasyRandom(parameters);
        return generator.nextObject(c);
    }

    protected <T> T createObject(Class<T> c) {
        if (generator == null) {
            EasyRandomParameters parameters = new EasyRandomParameters()
                    .randomize(CacheableConnector.class, DummyConnector::new);
            generator = new EasyRandom(parameters);
        }
        return generator.nextObject(c);
    }

    /**
     * Given a node number, create a list of random nodes
     */
    protected List<Node> getNodes(int nodeNum) {
        List<Node> nodeList = new ArrayList<>(nodeNum);
        for (int i = 0; i < nodeNum; i++) {
            Node node = createObject(generator, Node.class);
            node.setHash(null);
            nodeList.add(node);
        }
        return nodeList;
    }

    /**
     * Node is consistent hashed by index.
     * e.g. using 0,1,2 int as key should be put to index 0, 1, 2 nodes
     */
    protected void assertKeyDispatched(List<Node> nodeList, int amount) {
        for (int i = 0; i < amount; i++) { //3 times of node size
            Node selectNode = nodeManager.nodeGet(i);
            double keyHash = ConsistentHashUtil.myHash(i);
            double diff = Math.abs(keyHash - selectNode.getHash());
            Node maxHashNode = findMaxHashNode(nodeList);
            Node minHashNode = findMinHashNode(nodeList);
            if (keyHash > maxHashNode.getHash()) {
                //key is larger than max hash, should be put to min hash node
                assertThat(selectNode.getNodeId()).isEqualTo(minHashNode.getNodeId());
            } else {
                //key is smaller than max hash, should be put to node with hash closest to key hash
                for (Node node : nodeList) {
                    double diffToNode = Math.abs(keyHash - node.getHash());
                    assertThat(diff).isLessThanOrEqualTo(diffToNode);
                }
            }
        }
    }

}
