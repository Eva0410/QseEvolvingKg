package shape_comparator.data;

import cs.qse.common.structure.PS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.IRI;
import shape_comparator.services.Utils;

@Entity
public class PropertyShape {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long propertyShapeId;

    IRI iri;
    String path;
    String nodeKind;
    String dataTypeOrClass;
    Integer support;
    Double confidence;

    @Lob
    String generatedText;

    @ManyToOne
    @JoinColumn(name="nodeShapeId")
    NodeShape nodeShape;

    private PropertyShape(PS ps) {
        iri = ps.getIri();
        path = ps.getPath();
        nodeKind = ps.getNodeKind();
        dataTypeOrClass = ps.getDataTypeOrClass();
        support = ps.getSupport();
        confidence = ps.getConfidence();

        //set shaclOrItems (copied from Shactor)
        //Also resets support and confidence to the maximum confidence if ShaclOrItems are used (copied from Shactor)
        if(ps.getShaclOrListItems() != null && !ps.getShaclOrListItems().isEmpty()){
            cs.qse.common.structure.ShaclOrListItem maxConfidenceItem = null;
            for (var item:
                    ps.getShaclOrListItems()) {
                if (maxConfidenceItem == null) {
                    maxConfidenceItem = item;
                }
                if (item.getConfidence() > maxConfidenceItem.getConfidence()) {
                    maxConfidenceItem = item;
                }
            }
            support = maxConfidenceItem.getSupport();
            confidence = maxConfidenceItem.getConfidence();
        }
    }

    public PropertyShape(PS ps, NodeShape ns) {
        this(ps);
        this.nodeShape = ns;
        if(willPSbeAdded())
            this.generateText();
    }

    public Boolean willPSbeAdded() {
        //Bug in Shactor that all shapes are passed, no mather if support and confidence are correct
        return this.getSupport() > this.getNodeShape().getExtractedShapes().getSupport() && this.getConfidence()*100 > this.getNodeShape().getExtractedShapes().getConfidence();
    }

    public PropertyShape() {}

    public NodeShape getNodeShape() {
        return nodeShape;
    }

    public void setNodeShape(NodeShape nodeShape) {
        this.nodeShape = nodeShape;
    }

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

    public Long getPropertyShapeId() {
        return propertyShapeId;
    }

    public void setPropertyShapeId(Long id) {
        this.propertyShapeId = id;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
    }

    public void generateText() {
        if(this.nodeShape.shouldGenerateText) {
            this.generatedText = Utils.generateTTLFromRegex(iri, this.nodeShape.extractedShapes.getFileAsString(), this.nodeShape.extractedShapes.prefixLines);
        }
    }
}
