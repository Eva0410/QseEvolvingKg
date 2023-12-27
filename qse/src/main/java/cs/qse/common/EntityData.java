//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import cs.utils.Tuple2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityData {
    Set<Integer> classTypes = new HashSet();
    public Map<Integer, PropertyData> propertyConstraintsMap = new HashMap();

    public EntityData() {
    }

    public Set<Integer> getClassTypes() {
        return this.classTypes;
    }

    public Collection<Tuple2<Integer, Integer>> getPropertyConstraints() {
        List<Tuple2<Integer, Integer>> propertyConstraints = new ArrayList(this.propertyConstraintsMap.size() * 5 / 3);
        Iterator var2 = this.propertyConstraintsMap.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, PropertyData> pds = (Map.Entry)var2.next();
            Integer propertyID = (Integer)pds.getKey();
            PropertyData propertyData = (PropertyData)pds.getValue();
            Iterator var7 = propertyData.objTypes.iterator();

            while(var7.hasNext()) {
                Integer classType = (Integer)var7.next();
                Tuple2<Integer, Integer> pcs = new Tuple2(propertyID, classType);
                propertyConstraints.add(pcs);
            }
        }

        return propertyConstraints;
    }

    public void addPropertyConstraint(Integer propertyID, Integer classID) {
        PropertyData pd = (PropertyData)this.propertyConstraintsMap.get(propertyID);
        if (pd == null) {
            pd = new PropertyData();
            this.propertyConstraintsMap.put(propertyID, pd);
        }

        pd.objTypes.add(classID);
    }

    public void addPropertyCardinality(Integer propertyID) {
        PropertyData pd = (PropertyData)this.propertyConstraintsMap.get(propertyID);
        if (pd == null) {
            pd = new PropertyData();
            this.propertyConstraintsMap.put(propertyID, pd);
        }

        ++pd.count;
    }

    public static class PropertyData {
        Set<Integer> objTypes = new HashSet(5);
        public int count = 0;

        public PropertyData() {
        }
    }
}
