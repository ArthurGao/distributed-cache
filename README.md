# Overview

A distributed caching mechanism using a consistent hashing algorithm

* Cache is distributed across multiple nodes
* Support for multiple cache types (e.g., Redis, Memcached, etc.)
* Support for multiple cache key types (Implemented Serializable e.g. String, Integer, etc.)
* Support for multiple cache value types (e.g. String, Integer, etc.)
* Entries to be cached are dispatched to different nodes by consistent hashing algorithms
* Nodes are added or removed dynamically in cluster
* Support replicas of node
* Cached content will not be lost when node added/shutdown
* Node removed by accident will cause cached  content lost

# Build
Please run the following command to build the project:
```
mvn clean install
```
* All unit tests will be executed during the build process.


# Usage

Any spring-boot application can make use of this library to support gRPC common functionalities:

* **DistributedCache** - Distributed cache mechanism using consistent hashing algorithm which can be used to cache any type of data
* **NodeManager** - Node manager to manage nodes in cluster which can be used to add/remove/shutdown     nodes in cluster

1. **Include the dependency**

```
    <dependency>
      <groupId>com.unity</groupId>
      <artifactId>istributed-cache</artifactId>
      <version>1.0</version>
    </dependency>
```

2. **Manage nodes in cluster**
Initialize NodeManager with nodes in cluster(with replica number)

```java
    Node node1 = new Node("node1", 123, NodeType.REDIS);
    Node node2 = new Node("node2", 123, NodeType.MEMCACHED);
    NodeManager nodeManager = NodeManager.getInstance();
    nodeManager.initNodes(nodes, 3);
```

3. **Distribute cache**
Initialise a distributed cache with node manager

```java
    DistributedCache  distributedCache = new DistributedCache(nodeManager);
    distributedCache.put(key, value);
    distributedCache.get(key);
    distributedCache.remove(key);
```

4. **Add/Shutdown node(s)**

Node manager can be used to add/shutdown nodes in cluster

```java
    Node node3 = new Node("node3", 123, NodeType.REDIS);
    nodeManager.addNode(node3);
    nodeManager.shutdownNode(node3);
```
* Add node
    * The new node will be added to hashed circle as per its hash value
    * Cached content in previous/next nodes will be distributed to new node by consistent hashing algorithm
* Shutdown node
    * The node will be removed from hashed circle
    * Cached content in this node will be distributed to new node by consistent hashing algorithm which guarantees cached content will not be lost
 
5. **Remove node(s)**
Sometimes node is not working properly and need to be removed from cluster. In this case, cached content in this node will be lost.

```java
    nodeManager.removeNode(node3);
```

6. **Implement cache connector as per node type**
Implement connector for each node type to support cache operations

```java
    public class RedisCacheConnector implements CacheConnector {...}
    public class DummyMemcacheConnector implements CacheConnector {...}
```

7. **Java Runtime Exceptions**
Java Runtime Exceptions
- ServerInternalException
- IllegalArgumentException

8. **Unit Tests**
Unit tests are implemented to cover all the functionalities of this library. Please run the following command to execute all unit tests:
```
mvn test
```
* ConsistentHashUtilTest.java - Unit tests for consistent hashing algorithm
* NodeManagerTest.java - Unit tests for node manager  
* DistributedCacheTest.java - Unit tests for distributed cache

# References

* [Consistent hashing](https://web.archive.org/web/20221230083731/https:/michaelnielsen.org/blog/consistent-hashing/)