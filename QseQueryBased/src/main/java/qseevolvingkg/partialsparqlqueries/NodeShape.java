package qseevolvingkg.partialsparqlqueries;

import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

public class NodeShape {
    List<IRI> targetClasses;
    IRI iri;
    int support;

    public void addTargetClasses(IRI targetClasses) {
        if(this.targetClasses == null)
            this.targetClasses = new ArrayList<>();
        this.targetClasses.add(targetClasses);
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    @Override
    public String toString() {
        return "NodeShape{" +
                "targetClasses=" + targetClasses +
                ", iri=" + iri +
                ", support=" + support +
                '}';
    }
}
