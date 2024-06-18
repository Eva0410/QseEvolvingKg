package data;

import jakarta.persistence.*;

@Entity
public class ShaclOrListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    String nodeKind;
    String dataTypeOrClass;
    Integer support;
    Double confidence;

    public ShaclOrListItem() {
    }

    public ShaclOrListItem(String nodeKind, String dataTypeOrClass, Integer support, Double confidence) {
        this.nodeKind = nodeKind;
        this.dataTypeOrClass = dataTypeOrClass;
        this.support = support;
        this.confidence = confidence;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
