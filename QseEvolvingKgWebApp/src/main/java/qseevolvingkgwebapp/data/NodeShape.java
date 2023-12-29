package qseevolvingkgwebapp.data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.IRI;
import org.hibernate.mapping.Property;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<PropertyShape> propertyShapeList;

    @ManyToOne
    ExtractedShapes extractedShapes;

    public NodeShape() {}
    public NodeShape(NS ns) {
        this.iri = ns.getIri();
        this.targetClass = ns.getTargetClass();
        this.support = ns.getSupport();
        this.propertyShapeList = new ArrayList<>();
        for (var ps : ns.getPropertyShapes()) {
            propertyShapeList.add(new PropertyShape(ps, this));
        }
    }

    public NodeShape(NS ns, ExtractedShapes es) {
        this(ns);
        this.extractedShapes = es;
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
}
