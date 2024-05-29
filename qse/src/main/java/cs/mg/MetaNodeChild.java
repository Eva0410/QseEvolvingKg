
package cs.mg;

public class MetaNodeChild {
    Integer node;
    Integer frequency;
    Integer noc;

    public MetaNodeChild(Integer node, Integer frequency, Integer numberOfChildren) {
        this.node = node;
        this.frequency = frequency;
        this.noc = numberOfChildren;
    }

    public Integer getNode() {
        return this.node;
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    public Integer getNoc() {
        return this.noc;
    }
}
