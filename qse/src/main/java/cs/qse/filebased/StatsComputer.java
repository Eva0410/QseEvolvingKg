//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased;

import cs.Main;
import cs.qse.common.EntityData;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.yars.nx.Node;

public class StatsComputer {
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Set<Integer>> propWithClassesHavingMaxCountOne = new HashMap();
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    Boolean isSamplingOn = false;

    public StatsComputer() {
    }

    public void computeSupportConfidence(Map<Node, EntityData> entityDataHashMap, Map<Integer, Integer> classEntityCount) {
        entityDataHashMap.forEach((entity, entityData) -> {
            Set<Integer> instanceClasses = ((EntityData)entityDataHashMap.get(entity)).getClassTypes();
            if (instanceClasses != null) {
                Iterator var5 = instanceClasses.iterator();

                while(var5.hasNext()) {
                    Integer c = (Integer)var5.next();
                    Iterator var7 = entityData.getPropertyConstraints().iterator();

                    while(var7.hasNext()) {
                        Tuple2<Integer, Integer> propObjTuple = (Tuple2)var7.next();
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(c, (Integer)propObjTuple._1, (Integer)propObjTuple._2);
                        SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                        if (sc == null) {
                            this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                        } else {
                            Integer newSupp = sc.getSupport() + 1;
                            sc.setSupport(newSupp);
                            this.shapeTripletSupport.put(tuple3, sc);
                        }
                    }
                }
            }

            if (Main.extractMaxCardConstraints) {
                entityData.propertyConstraintsMap.forEach((property, propertyData) -> {
                    if (propertyData.count <= 1) {
                        this.propWithClassesHavingMaxCountOne.putIfAbsent(property, new HashSet());

                        assert instanceClasses != null;

                        ((Set)this.propWithClassesHavingMaxCountOne.get(property)).addAll(instanceClasses);
                    }

                });
            }

        });
        Iterator var3 = this.shapeTripletSupport.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var3.next();
            SupportConfidence value = (SupportConfidence)entry.getValue();
            double confidence = (double)value.getSupport() / (double)(Integer)classEntityCount.get(((Tuple3)entry.getKey())._1);
            value.setConfidence(confidence);
        }

    }

    public void computeSupportConfidenceWithEncodedEntities(Map<Integer, EntityData> entityDataMapReservoir, Map<Integer, Integer> classEntityCount) {
        entityDataMapReservoir.forEach((entity, entityData) -> {
            Set<Integer> instanceClasses = ((EntityData)entityDataMapReservoir.get(entity)).getClassTypes();
            if (instanceClasses != null) {
                Iterator var5 = instanceClasses.iterator();

                while(var5.hasNext()) {
                    Integer c = (Integer)var5.next();
                    Iterator var7 = entityData.getPropertyConstraints().iterator();

                    while(var7.hasNext()) {
                        Tuple2<Integer, Integer> propObjTuple = (Tuple2)var7.next();
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(c, (Integer)propObjTuple._1, (Integer)propObjTuple._2);
                        SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                        if (sc == null) {
                            this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                        } else {
                            Integer newSupp = sc.getSupport() + 1;
                            sc.setSupport(newSupp);
                            this.shapeTripletSupport.put(tuple3, sc);
                        }
                    }
                }
            }

            if (Main.extractMaxCardConstraints) {
                entityData.propertyConstraintsMap.forEach((property, propertyData) -> {
                    if (propertyData.count <= 1) {
                        this.propWithClassesHavingMaxCountOne.putIfAbsent(property, new HashSet());

                        assert instanceClasses != null;

                        ((Set)this.propWithClassesHavingMaxCountOne.get(property)).addAll(instanceClasses);
                    }

                });
            }

        });

        SupportConfidence value;
        double confidence;
        for(Iterator var3 = this.shapeTripletSupport.entrySet().iterator(); var3.hasNext(); value.setConfidence(confidence)) {
            Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var3.next();
            value = (SupportConfidence)entry.getValue();
            if (this.isSamplingOn) {
                confidence = (double)value.getSupport() / (double)((List)this.sampledEntitiesPerClass.get(((Tuple3)entry.getKey())._1)).size();
            } else {
                confidence = (double)value.getSupport() / (double)(Integer)classEntityCount.get(((Tuple3)entry.getKey())._1);
            }
        }

    }

    public void setShapeTripletSupport(Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport) {
        this.shapeTripletSupport = shapeTripletSupport;
    }

    public void setPropWithClassesHavingMaxCountOne(Map<Integer, Set<Integer>> propWithClassesHavingMaxCountOne) {
        this.propWithClassesHavingMaxCountOne = propWithClassesHavingMaxCountOne;
    }

    public Map<Integer, Set<Integer>> getPropWithClassesHavingMaxCountOne() {
        return this.propWithClassesHavingMaxCountOne;
    }

    public Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> getShapeTripletSupport() {
        return this.shapeTripletSupport;
    }

    public void computeSupportConfidence(Map<Node, HashSet<Tuple2<Integer, Integer>>> entityToPropertyConstraints, HashMap<Node, HashSet<Integer>> entityToClassTypes, HashMap<Integer, Integer> classEntityCount) {
        entityToPropertyConstraints.forEach((instance, propertyShapeSet) -> {
            HashSet<Integer> instanceClasses = (HashSet)entityToClassTypes.get(instance);
            if (instanceClasses != null) {
                Iterator var5 = ((HashSet)entityToClassTypes.get(instance)).iterator();

                while(var5.hasNext()) {
                    Integer c = (Integer)var5.next();
                    Iterator var7 = propertyShapeSet.iterator();

                    while(var7.hasNext()) {
                        Tuple2<Integer, Integer> propObjTuple = (Tuple2)var7.next();
                        Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(c, (Integer)propObjTuple._1, (Integer)propObjTuple._2);
                        if (this.shapeTripletSupport.containsKey(tuple3)) {
                            SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                            Integer newSupp = sc.getSupport() + 1;
                            sc.setSupport(newSupp);
                            this.shapeTripletSupport.put(tuple3, sc);
                        } else {
                            this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                        }
                    }
                }
            }

        });
        Iterator var4 = this.shapeTripletSupport.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var4.next();
            SupportConfidence value = (SupportConfidence)entry.getValue();
            double confidence = (double)value.getSupport() / (double)(Integer)classEntityCount.get(((Tuple3)entry.getKey())._1);
            value.setConfidence(confidence);
        }

    }

    public void setSampledEntityCount(Map<Integer, List<Integer>> sampledEntitiesPerClass) {
        this.sampledEntitiesPerClass = sampledEntitiesPerClass;
    }

    public void setSamplingOn(Boolean samplingOn) {
        this.isSamplingOn = samplingOn;
    }
}
