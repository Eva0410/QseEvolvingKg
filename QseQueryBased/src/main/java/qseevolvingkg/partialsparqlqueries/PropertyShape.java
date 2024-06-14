package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.PS;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import java.util.ArrayList;
import java.util.List;


//Copied from WebApp
public class PropertyShape {
    IRI iri;
    int support;
    double confidence;
    IRI nodeKind;
    IRI path;
    IRI dataType;
    IRI classIri;
    List<ShaclOrListItem> orItems = new ArrayList<>();
    boolean errorDuringGeneration = false;
    IRI dataTypeOrClass;
    public NodeShape nodeShape;

    public  PropertyShape() {

    }

    PropertyShape(PS ps, NodeShape nodeShape) {
        this.nodeShape = nodeShape;
        iri = ps.getIri();
        path = Values.iri(ps.getPath());
        nodeKind = getNodeKindFromQSE(ps.getNodeKind());
        dataTypeOrClass = ps.getDataTypeOrClass() == null ? null : Values.iri(ps.getDataTypeOrClass());
        support = getValueFromQse(ps.getSupport());
        confidence = getValueFromQse(ps.getConfidence());
        if(ps.getConfidence() == null)
            confidence = 0;
        else
            confidence = ps.getConfidence();

        //set shaclOrItems (copied from Shactor)
        //Also resets support and confidence to the maximum confidence if ShaclOrItems are used (copied from Shactor)
        if(ps.getShaclOrListItems() != null && !ps.getShaclOrListItems().isEmpty()){
            cs.qse.common.structure.ShaclOrListItem maxConfidenceItem = null;
            var shaclOrListItems = new ArrayList<ShaclOrListItem>();
            for (var item:
                    ps.getShaclOrListItems()) {
                shaclOrListItems.add(new ShaclOrListItem(item.getNodeKind(),item.getDataTypeOrClass(), item.getSupport(), item.getConfidence(), this));
                if (maxConfidenceItem == null) {
                    maxConfidenceItem = item;
                }
                if (item.getConfidence() > maxConfidenceItem.getConfidence()) {
                    maxConfidenceItem = item;
                }
            }
            support = maxConfidenceItem.getSupport();
            confidence = maxConfidenceItem.getConfidence();
            this.orItems = shaclOrListItems;
        }
    }

    public static IRI getNodeKindFromQSE(String nodeKind) {
        if(nodeKind != null && nodeKind.equals("Literal"))
            return Values.iri("http://www.w3.org/ns/shacl#Literal");
        if(nodeKind != null && nodeKind.equals("IRI"))
            return  Values.iri("http://www.w3.org/ns/shacl#IRI");
        return null;
    }

    public static int getValueFromQse(Integer value) {
        if(value == null)
            return 0;
        else
            return value;
    }

    public static double getValueFromQse(Double value) {
        if(value == null)
            return 0;
        else
            return value;
    }

    public PropertyShape(IRI iri, int support, double confidence) {
        this.iri = iri;
        this.support = support;
        this.confidence = confidence;
    }

    public void addOrListItem(IRI nodeKind, IRI classIri, IRI dataType) {
        if(this.orItems == null)
            this.orItems = new ArrayList<>();
        this.orItems.add(new ShaclOrListItem(nodeKind, classIri, dataType));
    }
}
