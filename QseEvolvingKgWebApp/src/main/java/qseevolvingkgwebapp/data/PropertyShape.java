package qseevolvingkgwebapp.data;

import cs.qse.common.structure.PS;
import cs.qse.common.structure.ShaclOrListItem;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;

@Entity
public class PropertyShape {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    IRI iri;
    String path;
    String nodeKind;
    String dataTypeOrClass;
    Integer support;
    Double confidence;
//    List<ShaclOrListItem> shaclOrListItems;

    public PropertyShape(PS ps) {
        iri = ps.getIri();
        path = ps.getPath();
        nodeKind = ps.getNodeKind();
        dataTypeOrClass = ps.getDataTypeOrClass();
        support = ps.getSupport();
        confidence = ps.getConfidence();
//        shaclOrListItems = ps.getShaclOrListItems();
    }
    public PropertyShape() {}


    public IRI getIri() {
        return iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNodeKind() {
        return nodeKind;
    }

    public void setNodeKind(String nodeKind) {
        this.nodeKind = nodeKind;
    }

    public String getDataTypeOrClass() {
        return dataTypeOrClass;
    }

    public void setDataTypeOrClass(String dataTypeOrClass) {
        this.dataTypeOrClass = dataTypeOrClass;
    }

    public Integer getSupport() {
        return support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

//    public List<ShaclOrListItem> getShaclOrListItems() {
//        return shaclOrListItems;
//    }
//
//    public void setShaclOrListItems(List<ShaclOrListItem> shaclOrListItems) {
//        this.shaclOrListItems = shaclOrListItems;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
