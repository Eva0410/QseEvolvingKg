//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.semanticweb.yars.nx.Node;

public class LRUCache {
    private LinkedHashMap<Node, List<Node>> cacheMap;

    public LRUCache(final int capacity) {
        this.cacheMap = new LinkedHashMap<Node, List<Node>>(capacity, 0.75F, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > capacity;
            }
        };
    }

    public List<Node> get(Node key) {
        return (List)this.cacheMap.get(key);
    }

    public void put(Node key, List<Node> value) {
        this.cacheMap.put(key, value);
    }

    public boolean containsKey(Node node) {
        return this.cacheMap.get(node) != null;
    }
}
