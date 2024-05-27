//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.structure;

import java.util.List;
import org.eclipse.rdf4j.model.IRI;

public class PS {
    IRI iri;
    String path;
    String nodeKind;
    String dataTypeOrClass;
    Integer support;
    Double confidence;
    List<ShaclOrListItem> shaclOrListItems;
    Boolean hasOrList = false;
    Boolean pruneFlag = false;
    Boolean supportPruneFlag = false;
    Boolean confidencePruneFlag = false;

    public PS() {
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

    public String getLocalNameFromIri() {
        return this.iri.getLocalName();
    }

    public Boolean getPruneFlag() {
        return this.pruneFlag;
    }

    public void setPruneFlag(Boolean pruneFlag) {
        this.pruneFlag = pruneFlag;
    }

    public IRI getIri() {
        return this.iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public Boolean getHasOrList() {
        return this.hasOrList;
    }

    public void setHasOrList(Boolean hasOrList) {
        this.hasOrList = hasOrList;
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

    public List<ShaclOrListItem> getShaclOrListItems() {
        return this.shaclOrListItems;
    }

    public void setShaclOrListItems(List<ShaclOrListItem> shaclOrListItems) {
        this.shaclOrListItems = shaclOrListItems;
    }
}
