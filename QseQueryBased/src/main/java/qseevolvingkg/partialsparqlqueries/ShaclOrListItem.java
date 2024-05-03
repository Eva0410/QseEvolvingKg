package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;

public class ShaclOrListItem {
    IRI nodeKind;
    IRI classIri;
    int support;
    public ShaclOrListItem() {

    }

    public ShaclOrListItem(IRI nodeKind, IRI classIri) {
        this.nodeKind = nodeKind;
        this.classIri = classIri;
    }
}
