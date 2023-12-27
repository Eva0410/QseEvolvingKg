//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.sampling.parallel;

import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.common.Utility;
import cs.qse.common.encoders.ConcurrentStringEncoder;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import cs.utils.Utils;
import cs.utils.graphdb.GraphDBUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.query.BindingSet;
import org.semanticweb.yars.nx.Node;

public class SubEntityPropDataCollector {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();
    Map<Integer, EntityData> subEntityDataMapContainer;
    String typePredicate;
    ConcurrentStringEncoder prevEncoder;
    NodeEncoder prevNodeEncoder;
    Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes;
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Integer> sampledPropCount;

    public SubEntityPropDataCollector(Integer expectedNumberOfClasses) {
        this.classToPropWithObjTypes = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.shapeTripletSupport = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.sampledPropCount = new HashMap(13334);
    }

    public void setFeatures(String typePredicate, ConcurrentStringEncoder prevEncoder, NodeEncoder prevNodeEncoder) {
        this.typePredicate = typePredicate;
        this.prevEncoder = prevEncoder;
        this.prevNodeEncoder = prevNodeEncoder;
    }

    public void job(Integer jobIndex, List<Integer> entities, Map<Integer, EntityData> entityDataMapContainer) {
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println("Started Job(index=" + jobIndex + ") in Thread : " + Thread.currentThread().getName());
        this.subEntityDataMapContainer = new HashMap();

        try {
            entities.forEach((entityID) -> {
                EntityData entityData = (EntityData)entityDataMapContainer.get(entityID);
                this.subEntityDataMapContainer.put(entityID, entityData);
                Set<String> entityTypes = new HashSet();
                Iterator var5 = entityData.getClassTypes().iterator();

                while(var5.hasNext()) {
                    Integer entityTypeIDx = (Integer)var5.next();
                    entityTypes.add(this.prevEncoder.decode(entityTypeIDx));
                }

                String entity = this.prevNodeEncoder.decode(entityID).getLabel();
                String query = Utility.buildQuery(entity, entityTypes, this.typePredicate);
                Iterator var7 = this.graphDBUtils.evaluateSelectQuery(query).iterator();

                while(var7.hasNext()) {
                    BindingSet row = (BindingSet)var7.next();
                    String prop = row.getValue("p").stringValue();
                    String obj = row.getValue("o").stringValue();
                    int propID = this.prevEncoder.encode(prop);
                    Node objNode = Utils.IriToNode(obj);
                    String objType = Utility.extractObjectType(obj);
                    Set<Integer> objTypesIDs = new HashSet(10);
                    Set<Tuple2<Integer, Integer>> prop2objTypeTuplesx = new HashSet(10);
                    if (!objType.equals("IRI")) {
                        int objID;
                        if (this.prevEncoder.isEncoded(objType)) {
                            objID = this.prevEncoder.encode(objType);
                        } else {
                            objID = this.prevEncoder.encode(objType);
                        }

                        ((Set)objTypesIDs).add(objID);
                        Set<Tuple2<Integer, Integer>> prop2objTypeTuples = Collections.singleton(new Tuple2(propID, objID));
                        this.addEntityToPropertyConstraints(prop2objTypeTuples, entityID);
                    } else {
                        EntityData currEntityData = (EntityData)entityDataMapContainer.get(this.prevNodeEncoder.getEncodedNode(objNode));
                        if (currEntityData == null) {
                            ((Set)objTypesIDs).add(-1);
                        } else {
                            objTypesIDs = currEntityData.getClassTypes();
                            Iterator var17 = ((Set)objTypesIDs).iterator();

                            while(var17.hasNext()) {
                                Integer objTypeID = (Integer)var17.next();
                                prop2objTypeTuplesx.add(new Tuple2(propID, objTypeID));
                            }

                            this.addEntityToPropertyConstraints(prop2objTypeTuplesx, entityID);
                        }
                    }

                    Iterator var24 = entityData.getClassTypes().iterator();

                    while(var24.hasNext()) {
                        Integer entityTypeID = (Integer)var24.next();
                        Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.get(entityTypeID);
                        if (propToObjTypes == null) {
                            propToObjTypes = new HashMap();
                            this.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                        }

                        Set<Integer> classObjTypes = (Set)((Map)propToObjTypes).get(propID);
                        if (classObjTypes == null) {
                            classObjTypes = new HashSet();
                            ((Map)propToObjTypes).put(propID, classObjTypes);
                        }

                        ((Set)classObjTypes).addAll((Collection)objTypesIDs);
                        ((Map)propToObjTypes).put(propID, classObjTypes);
                        this.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                        ((Set)objTypesIDs).forEach((objTypeIDx) -> {
                            Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(entityTypeID, propID, objTypeIDx);
                            if (this.shapeTripletSupport.containsKey(tuple3)) {
                                Integer support = ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport();
                                support = support + 1;
                                this.shapeTripletSupport.put(tuple3, new SupportConfidence(support));
                            } else {
                                this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                            }

                        });
                        this.sampledPropCount.merge(propID, 1, Integer::sum);
                    }
                }

            });
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        watch.stop();
        System.out.println("\nFinished Job(index=" + jobIndex + ") in " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + "seconds or " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()) + " minutes");
    }

    private void addEntityToPropertyConstraints(Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Integer subject) {
        EntityData currentEntityData = (EntityData)this.subEntityDataMapContainer.get(subject);
        if (currentEntityData == null) {
            currentEntityData = new EntityData();
        }

        Iterator var4 = prop2objTypeTuples.iterator();

        while(var4.hasNext()) {
            Tuple2<Integer, Integer> tuple2 = (Tuple2)var4.next();
            currentEntityData.addPropertyConstraint((Integer)tuple2._1, (Integer)tuple2._2);
            if (Main.extractMaxCardConstraints) {
                currentEntityData.addPropertyCardinality((Integer)tuple2._1);
            }
        }

        this.subEntityDataMapContainer.put(subject, currentEntityData);
    }
}
