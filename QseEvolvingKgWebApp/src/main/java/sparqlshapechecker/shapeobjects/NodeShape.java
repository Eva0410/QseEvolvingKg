package sparqlshapechecker.shapeobjects;

import cs.qse.common.structure.NS;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.List;

//Copied from WebApp
public class NodeShape {
    public IRI targetClass;
    public IRI iri;
    public String iriLocalName;
    public int support;
    public List<PropertyShape> propertyShapes;
    public boolean errorDuringGeneration = false;
    public ExtractedShapes extractedShapes;

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
            if(propertyShape.support > extractedShapes.support && propertyShape.confidence*100 > extractedShapes.confidence)
                propertyShapes.add(propertyShape);

        }
        this.iriLocalName = iri.getLocalName();
    }

    public IRI getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(IRI targetClass) {
        this.targetClass = targetClass;
    }

    public IRI getIri() {
        return iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public String getIriLocalName() {
        return iriLocalName;
    }

    public void setIriLocalName(String iriLocalName) {
        this.iriLocalName = iriLocalName;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }


}
