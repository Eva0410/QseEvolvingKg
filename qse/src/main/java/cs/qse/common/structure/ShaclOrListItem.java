//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.structure;

public class ShaclOrListItem {
    String nodeKind;
    String dataTypeOrClass;
    Integer support;
    Double confidence;
    Boolean pruneFlag = false;
    Boolean supportPruneFlag = false;
    Boolean confidencePruneFlag = false;

    public ShaclOrListItem() {
    }

    public Boolean getSupportPruneFlag() {
        return this.supportPruneFlag;
    }

    public void setSupportPruneFlag(Boolean supportPruneFlag) {
        this.supportPruneFlag = supportPruneFlag;
    }

    public Boolean getConfidencePruneFlag() {
        return this.confidencePruneFlag;
    }

    public void setConfidencePruneFlag(Boolean confidencePruneFlag) {
        this.confidencePruneFlag = confidencePruneFlag;
    }

    public Boolean getPruneFlag() {
        return this.pruneFlag;
    }

    public void setPruneFlag(Boolean pruneFlag) {
        this.pruneFlag = pruneFlag;
    }

    public String getNodeKind() {
        return this.nodeKind;
    }

    public void setNodeKind(String nodeKind) {
        this.nodeKind = nodeKind;
    }

    public String getDataTypeOrClass() {
        return this.dataTypeOrClass;
    }

    public void setDataTypeOrClass(String dataTypeOrClass) {
        this.dataTypeOrClass = dataTypeOrClass;
    }

    public Integer getSupport() {
        return this.support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public String getConfidenceInPercentage() {
        if (this.confidence != null) {
            int c = (int)(this.confidence * 100.0);
            return "" + c + " %";
        } else {
            return "-";
        }
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
