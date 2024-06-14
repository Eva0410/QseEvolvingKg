package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

public class ShaclOrListItem {
    IRI nodeKind;
    IRI classIri;
    int support = 0;
    Double confidence = 0.0;
    IRI dataType;
    IRI dataTypeOrClass;
    boolean errorDuringGeneration = false;
    public PropertyShape propertyShape;

    public ShaclOrListItem() {

    }

    //unused
    public ShaclOrListItem(IRI nodeKind, IRI classIri, IRI dataType) {
        this.nodeKind = nodeKind;
        this.classIri = classIri;
        this.dataType = dataType;
    }

    public ShaclOrListItem(String nodeKind, String dataTypeOrClass, Integer support, Double confidence, PropertyShape propertyShape) {
        this.nodeKind = PropertyShape.getNodeKindFromQSE(nodeKind);
        this.dataTypeOrClass = dataTypeOrClass == null || dataTypeOrClass.isEmpty() || dataTypeOrClass.isBlank() ? null : Values.iri(dataTypeOrClass);
        this.support = PropertyShape.getValueFromQse(support);
        this.confidence = PropertyShape.getValueFromQse(confidence);
        this.propertyShape = propertyShape;
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
