package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

public class ShaclOrListItem {
    IRI nodeKind;
    IRI classIri;
    int support;
    IRI dataType;
    String dataTypeOrClass;
    boolean errorDuringGeneration = false;
    Double confidence;

    public ShaclOrListItem() {

    }

    public ShaclOrListItem(IRI nodeKind, IRI classIri, IRI dataType) {
        this.nodeKind = nodeKind;
        this.classIri = classIri;
        this.dataType = dataType;
    }

    public ShaclOrListItem(String nodeKind, String dataTypeOrClass, Integer support, Double confidence) {
        this.nodeKind = PropertyShape.getNodeKindFromQSE(nodeKind);
        this.dataTypeOrClass = dataTypeOrClass;
        this.support = PropertyShape.getValueFromQse(support);
        this.confidence = PropertyShape.getValueFromQse(confidence);
    }

    @Override
    public String toString() {
        return "ShaclOrListItem{" +
                "nodeKind=" + nodeKind +
                ", classIri=" + classIri +
                ", support=" + support +
                ", dataType=" + dataType +
                ", errorDuringGeneration=" + errorDuringGeneration +
                '}';
    }
}
