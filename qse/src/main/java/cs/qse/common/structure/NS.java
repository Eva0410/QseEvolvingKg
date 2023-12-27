//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common.structure;

import java.util.Iterator;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;

public class NS {
    IRI iri;
    IRI targetClass;
    Integer support;
    List<PS> propertyShapes;
    Integer countPropertyShapes;
    Boolean pruneFlag = false;
    Integer countPsWithPruneFlag;
    Integer countPscWithPruneFlag;
    Integer countPsWithSupportPruneFlag;
    Integer countPscWithSupportPruneFlag;
    Integer countPsWithConfidencePruneFlag;
    Integer countPscWithConfidencePruneFlag;

    public NS() {
    }

    public Integer getCountPsWithPruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(var2.hasNext()) {
            PS ps = (PS)var2.next();
            if (ps.getPruneFlag()) {
                count = count + 1;
            }
        }

        return count;
    }

    public Integer getCountPsWithSupportPruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(var2.hasNext()) {
            PS ps = (PS)var2.next();
            if (ps.getSupportPruneFlag()) {
                count = count + 1;
            }
        }

        return count;
    }

    public Integer getCountPsWithConfidencePruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(var2.hasNext()) {
            PS ps = (PS)var2.next();
            if (ps.getConfidencePruneFlag()) {
                count = count + 1;
            }
        }

        return count;
    }

    public Integer getCountPscWithPruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(true) {
            PS ps;
            do {
                if (!var2.hasNext()) {
                    return count;
                }

                ps = (PS)var2.next();
            } while(ps.getShaclOrListItems() == null);

            Iterator var4 = ps.getShaclOrListItems().iterator();

            while(var4.hasNext()) {
                ShaclOrListItem item = (ShaclOrListItem)var4.next();
                if (item.getPruneFlag()) {
                    count = count + 1;
                }
            }
        }
    }

    public Integer getCountPscWithSupportPruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(true) {
            PS ps;
            do {
                if (!var2.hasNext()) {
                    return count;
                }

                ps = (PS)var2.next();
            } while(ps.getShaclOrListItems() == null);

            Iterator var4 = ps.getShaclOrListItems().iterator();

            while(var4.hasNext()) {
                ShaclOrListItem item = (ShaclOrListItem)var4.next();
                if (item.getSupportPruneFlag()) {
                    count = count + 1;
                }
            }
        }
    }

    public Integer getCountPscWithConfidencePruneFlag() {
        Integer count = 0;
        Iterator var2 = this.getPropertyShapes().iterator();

        while(true) {
            PS ps;
            do {
                if (!var2.hasNext()) {
                    return count;
                }

                ps = (PS)var2.next();
            } while(ps.getShaclOrListItems() == null);

            Iterator var4 = ps.getShaclOrListItems().iterator();

            while(var4.hasNext()) {
                ShaclOrListItem item = (ShaclOrListItem)var4.next();
                if (item.getConfidencePruneFlag()) {
                    count = count + 1;
                }
            }
        }
    }

    public Boolean getPruneFlag() {
        return this.pruneFlag;
    }

    public void setPruneFlag(Boolean pruneFlag) {
        this.pruneFlag = pruneFlag;
    }

    public IRI getIri() {
        return this.iri;
    }

    public void setIri(IRI iri) {
        this.iri = iri;
    }

    public String getLocalNameFromIri() {
        return this.iri.getLocalName();
    }

    public IRI getTargetClass() {
        return this.targetClass;
    }

    public void setTargetClass(IRI targetClass) {
        this.targetClass = targetClass;
    }

    public Integer getSupport() {
        return this.support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public List<PS> getPropertyShapes() {
        return this.propertyShapes;
    }

    public void setPropertyShapes(List<PS> propertyShapes) {
        this.propertyShapes = propertyShapes;
    }

    public Integer getCountPropertyShapes() {
        return this.propertyShapes.size();
    }

    public void setCountPropertyShapes() {
        this.countPropertyShapes = this.propertyShapes.size();
    }
}
