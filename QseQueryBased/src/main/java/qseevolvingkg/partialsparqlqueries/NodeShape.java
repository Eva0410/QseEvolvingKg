package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.NS;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

//Copied from WebApp
public class NodeShape {
    IRI targetClass;
    IRI iri;
    String iriLocalName;
    int support;
    List<PropertyShape> propertyShapes;
    boolean errorDuringGeneration = false;
    ExtractedShapes extractedShapes;

    public NodeShape() {

    }

    public NodeShape(NS ns, ExtractedShapes extractedShapes) {
        this.iri = ns.getIri();
        this.extractedShapes = extractedShapes;
        this.targetClass = ns.getTargetClass();
        this.support = ns.getSupport();
        this.propertyShapes = new ArrayList<>();
        for (var ps : ns.getPropertyShapes()) {
            //Bug in Shactor: if all classes are selected, all shapes will be returned, even when support and confidence
            //are not high enough
            var propertyShape = new PropertyShape(ps, this);
            propertyShape.nodeShape = this;
            //Bug in Shactor again: list of Propertyshapes contain objects which are not in the .SHACL file -> check for "pruned"-flag
            if(propertyShape.getSupport() > extractedShapes.support && propertyShape.getConfidence()*100 > extractedShapes.confidence)
                propertyShapes.add(propertyShape);

        }
        this.iriLocalName = iri.getLocalName();
    }
}
