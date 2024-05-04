package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

public class PropertyShape {
    IRI iri;
    int support;
    double confidence; //unused
    IRI nodeKind;
    IRI dataType;
    IRI path;
    IRI classIri;
    List<ShaclOrListItem> orItems;


    public  PropertyShape() {

    }

    public PropertyShape(IRI iri, int support, double confidence) {
        this.iri = iri;
        this.support = support;
        this.confidence = confidence;
    }

    public void addOrListItem(IRI nodeKind, IRI classIri, IRI dataType) {
        if(this.orItems == null)
            this.orItems = new ArrayList<>();
        this.orItems.add(new ShaclOrListItem(nodeKind, classIri, dataType));
    }

    public IRI getIri() {
        return iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
