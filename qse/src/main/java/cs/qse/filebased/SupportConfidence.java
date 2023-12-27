//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased;

public class SupportConfidence {
    Integer support = 0;
    Double confidence = 0.0;

    public SupportConfidence() {
    }

    public SupportConfidence(Integer support) {
        this.support = support;
    }

    public SupportConfidence(Integer s, Double c) {
        this.support = s;
        this.confidence = c;
    }

    public Integer getSupport() {
        return this.support;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String toString() {
        return "SupportConfidence{support=" + this.support + ", confidence=" + this.confidence + "}";
    }
}
