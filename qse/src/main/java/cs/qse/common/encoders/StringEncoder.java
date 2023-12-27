//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.encoders;

import java.util.HashMap;

public class StringEncoder implements Encoder {
    int counter;
    HashMap<Integer, String> table;
    HashMap<String, Integer> reverseTable;

    public StringEncoder() {
        this.counter = -1;
        this.table = new HashMap();
        this.reverseTable = new HashMap();
    }

    public StringEncoder(int counter, HashMap<Integer, String> table, HashMap<String, Integer> reverseTable) {
        this.counter = counter;
        this.table = table;
        this.reverseTable = reverseTable;
    }

    public int encode(String val) {
        if (this.reverseTable.containsKey(val)) {
            return (Integer)this.reverseTable.get(val);
        } else {
            ++this.counter;
            this.table.put(this.counter, val);
            this.reverseTable.put(val, this.counter);
            return this.counter;
        }
    }

    public boolean isEncoded(String val) {
        return this.reverseTable.containsKey(val);
    }

    public HashMap<Integer, String> getTable() {
        return this.table;
    }

    public String decode(int val) {
        return (String)this.table.get(val);
    }

    public HashMap<String, Integer> getRevTable() {
        return this.reverseTable;
    }

    public HashMap<String, Integer> getReverseTable() {
        return this.reverseTable;
    }
}
