package qseevolvingkgwebapp.data;

import cs.qse.common.structure.PS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.IRI;
import qseevolvingkgwebapp.services.Utils;

import java.util.ArrayList;
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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<ShaclOrListItem> shaclOrListItems;

    @Lob
    String generatedText;

    @ManyToOne
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
        if(ps.getShaclOrListItems() != null && ps.getShaclOrListItems().size() != 0){
            cs.qse.common.structure.ShaclOrListItem maxConfidenceItem = null;
            var shaclOrListItems = new ArrayList<ShaclOrListItem>();
            for (var item:
                    ps.getShaclOrListItems()) {
                shaclOrListItems.add(new ShaclOrListItem(item.getNodeKind(),item.getDataTypeOrClass(), item.getSupport(), item.getConfidence()));
                if (maxConfidenceItem == null) {
                    maxConfidenceItem = item;
                }
                if (item.getConfidence() > maxConfidenceItem.getConfidence()) {
                    maxConfidenceItem = item;
                }
            }
            support = maxConfidenceItem.getSupport();
            confidence = maxConfidenceItem.getConfidence();
            shaclOrListItems = shaclOrListItems;
        }
    }

    public PropertyShape(PS ps, NodeShape ns, boolean shouldGenerateText) {
        this(ps);
        this.nodeShape = ns;
        this.generateText();
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

    public List<ShaclOrListItem> getShaclOrListItems() {
        return shaclOrListItems;
    }

    public void setShaclOrListItems(List<ShaclOrListItem> shaclOrListItems) {
        this.shaclOrListItems = shaclOrListItems;
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
        if(this.nodeShape.shouldGenerateText) {
            var model = this.nodeShape.extractedShapes.getModel();
            this.generatedText = Utils.generateTTLFromIRIInModel(iri, model);
        }
    }
}
