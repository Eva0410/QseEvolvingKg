//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.encoders;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentStringEncoder implements Encoder {
    int counter = -1;
    ConcurrentHashMap<Integer, String> table = new ConcurrentHashMap();
    ConcurrentHashMap<String, Integer> reverseTable = new ConcurrentHashMap();

    public ConcurrentStringEncoder() {
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

    public ConcurrentHashMap<Integer, String> getTable() {
        return this.table;
    }

    public String decode(int val) {
        return (String)this.table.get(val);
    }

    public ConcurrentHashMap<String, Integer> getRevTable() {
        return this.reverseTable;
    }
}
