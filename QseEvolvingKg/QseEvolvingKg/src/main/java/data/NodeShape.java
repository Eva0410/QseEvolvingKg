package data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.IRI;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

//Did not want to use NS from QSE
@Entity
public class NodeShape {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    IRI iri;
    IRI targetClass;
    Integer support;
    String iriLocalName;
    @Lob
    String generatedText;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<PropertyShape> propertyShapeList;

    @ManyToOne
    ExtractedShapes extractedShapes;

    Boolean shouldGenerateText;

    public NodeShape() {}

    public NodeShape(NS ns, ExtractedShapes es, boolean shouldGenerateText) {
        this.iri = ns.getIri();
        this.targetClass = ns.getTargetClass();
        this.support = ns.getSupport();
        this.propertyShapeList = new ArrayList<>();
        this.extractedShapes = es;
        this.shouldGenerateText = shouldGenerateText;
        for (var ps : ns.getPropertyShapes()) {
            //Bug in Shactor: if all classes are selected, all shapes will be returned, even when support and confidence
            //are not high enough
            var propertyShape = new PropertyShape(ps, this);
            //Bug in Shactor again: list of Propertyshapes contain objects which are not in the .SHACL file
            if(propertyShape.willPSbeAdded() && propertyShape.getGeneratedText() != null && !propertyShape.getGeneratedText().isEmpty())
                propertyShapeList.add(propertyShape);
//            else
//                System.out.println(ps.getIri() + " dropped"); //Uncomment for debugging

            //special case for default shapes: should be added anyways
            if(!this.shouldGenerateText)
                propertyShapeList.add(propertyShape);

        }
        this.generateText();
        this.iriLocalName = iri.getLocalName();
    }

    public ExtractedShapes getExtractedShapes() {
        return extractedShapes;
    }

    public void setExtractedShapes(ExtractedShapes extractedShapes) {
        this.extractedShapes = extractedShapes;
    }

    public IRI getIri() {
        return iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public IRI getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(IRI targetClass) {
        this.targetClass = targetClass;
    }

    public Integer getSupport() {
        return support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public List<PropertyShape> getPropertyShapeList() {
        return propertyShapeList;
    }

    public void setPropertyShapeList(List<PropertyShape> propertyShapeList) {
        this.propertyShapeList = propertyShapeList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
    }

    public String getIriLocalName() {
        return iriLocalName;
    }

    public void generateText() {
        if(shouldGenerateText) {
//            var model = this.getExtractedShapes().getModelJena(); //for alternatives
            this.generatedText = Utils.generateTTLFromRegex(iri, this.extractedShapes.getFileAsString(), this.extractedShapes.prefixLines);
        }
    }
}
