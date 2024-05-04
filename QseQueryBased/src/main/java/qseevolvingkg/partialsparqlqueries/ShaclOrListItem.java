package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;

public class ShaclOrListItem {
    IRI nodeKind;
    IRI classIri;
    int support;
    IRI dataType;
    public ShaclOrListItem() {

    }

    public ShaclOrListItem(IRI nodeKind, IRI classIri, IRI dataType) {
        this.nodeKind = nodeKind;
        this.classIri = classIri;
        this.dataType = dataType;
    }
}
