package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.NS;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

public class NodeShape {
    List<IRI> targetClasses;
    IRI targetClass;
    IRI iri;
    String iriLocalName;

    int support;
    List<PropertyShape> propertyShapes;
    boolean errorDuringGeneration = false;
    String generatedText;

    public NodeShape() {

    }


    public NodeShape(NS ns) {
        this.iri = ns.getIri();
        this.targetClass = ns.getTargetClass();
        this.support = ns.getSupport();
        this.propertyShapes = new ArrayList<>();
        for (var ps : ns.getPropertyShapes()) {
            //Bug in Shactor: if all classes are selected, all shapes will be returned, even when support and confidence
            //are not high enough
            var propertyShape = new PropertyShape(ps);
            //Bug in Shactor again: list of Propertyshapes contain objects which are not in the .SHACL file -> check for "pruned"-flag
//            if(propertyShape.willPSbeAdded() && propertyShape.getGeneratedText() != null && !propertyShape.getGeneratedText().isEmpty())
            propertyShapes.add(propertyShape);

        }
        this.iriLocalName = iri.getLocalName();
    }
    public List<PropertyShape> getPropertyShapes() {
        return propertyShapes;
    }

    public void setPropertyShapes(List<PropertyShape> propertyShapes) {
        this.propertyShapes = propertyShapes;
    }

    //not used anymore, only one targetclass is supported
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
}
