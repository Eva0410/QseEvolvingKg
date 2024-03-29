package qseevolvingkgwebapp.data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import qseevolvingkgwebapp.services.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Lob
    String generatedText;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<PropertyShape> propertyShapeList;

    @ManyToOne
    ExtractedShapes extractedShapes;

    Boolean shouldGenerateText;

    @Transient
    Model filteredModel;

    public Model getFilteredModel() {
        if(filteredModel == null) {
            if(this.extractedShapes.getModel()==null)
                return null;
//            var tmp = this.extractedShapes.getModel().filter(BNode, null, null);
            this.filteredModel = this.extractedShapes.getModel().filter(this.getIri(),null,null);
        }
        return this.filteredModel;
    }

    public NodeShape() {}

    public NodeShape(NS ns, ExtractedShapes es, boolean shouldGenerateText) {
        long startTimeMillis = System.currentTimeMillis();

        this.iri = ns.getIri();
        this.targetClass = ns.getTargetClass();
        this.support = ns.getSupport();
        this.propertyShapeList = new ArrayList<>();
        this.extractedShapes = es;
        this.shouldGenerateText = shouldGenerateText;
        for (var ps : ns.getPropertyShapes()) {
            //Bug in Shactor...
            var propertyShape = new PropertyShape(ps, this, shouldGenerateText);
            if(propertyShape.getSupport() > extractedShapes.getSupport() && propertyShape.getConfidence()*100 > extractedShapes.getConfidence())
                propertyShapeList.add(propertyShape);
            else
                System.out.println(ps.getIri() + " dropped");
        }
        this.generateText();
        long durationMillis = System.currentTimeMillis() - startTimeMillis;
        System.out.println(this.getIri().getLocalName()+" "+durationMillis);
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

    public void generateText() {
        if(shouldGenerateText) {
            var model = this.getExtractedShapes().getModel();
            this.generatedText = Utils.generateTTLFromIRIInModel(iri, model);
        }
    }
}
