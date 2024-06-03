package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.PS;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import java.util.ArrayList;
import java.util.List;


//TODO use from Web-App?
public class PropertyShape {
    IRI iri;
    int support;
    double confidence; //unused
    IRI nodeKind;
    IRI path;
    IRI dataType;

    IRI classIri;
    List<ShaclOrListItem> orItems;
    boolean errorDuringGeneration = false;
    IRI dataTypeOrClass;
    List<ShaclOrListItem> shaclOrListItems; //never used

    PropertyShape(PS ps) {
        iri = ps.getIri();
        path = Values.iri(ps.getPath());
        nodeKind = getNodeKindFromQSE(ps.getNodeKind());
        dataTypeOrClass = Values.iri(ps.getDataTypeOrClass()); //todo works?
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
            this.shaclOrListItems = shaclOrListItems;
        }
    }

    public static IRI getNodeKindFromQSE(String nodeKind) {
        //todo replace with static strings
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

    public  PropertyShape() {

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

    public IRI getIri() {
        return iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
