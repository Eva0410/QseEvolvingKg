//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.encoders;

import java.util.HashMap;
import org.semanticweb.yars.nx.Node;

public class NodeEncoder {
    public int counter;
    HashMap<Integer, Node> table;
    HashMap<Node, Integer> reverseTable;

    public NodeEncoder() {
        this.counter = -1;
        this.table = new HashMap();
        this.reverseTable = new HashMap();
    }

    public NodeEncoder(int counter, HashMap<Integer, Node> table, HashMap<Node, Integer> reverseTable) {
        this.counter = counter;
        this.table = table;
        this.reverseTable = reverseTable;
    }

    public int encode(Node val) {
        if (this.reverseTable.containsKey(val)) {
            return (Integer)this.reverseTable.get(val);
        } else {
            ++this.counter;
            this.table.put(this.counter, val);
            this.reverseTable.put(val, this.counter);
            return this.counter;
        }
    }

    public int getEncodedNode(Node val) {
        int toReturn;
        if (this.reverseTable.get(val) == null) {
            toReturn = -9999;
        } else {
            toReturn = (Integer)this.reverseTable.get(val);
        }

        return toReturn;
    }

    public boolean remove(int val) {
        boolean returnVal = true;
        if (this.table.containsKey(val)) {
            this.reverseTable.remove(this.table.get(val));
            this.table.remove(val);
        } else {
            returnVal = false;
        }

        return returnVal;
    }

    public Node decode(int val) {
        return (Node)this.table.get(val);
    }

    public HashMap<Integer, Node> getTable() {
        return this.table;
    }

    public HashMap<Node, Integer> getReverseTable() {
        return this.reverseTable;
    }

    public boolean isNodeExists(Node node) {
        return this.reverseTable.get(node) != null;
    }
}
