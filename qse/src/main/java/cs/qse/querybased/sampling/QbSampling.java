//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.sampling;

import com.google.common.collect.Lists;
import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.common.ExperimentsUtil;
import cs.qse.common.ShapesExtractor;
import cs.qse.common.Utility;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.qse.filebased.sampling.DynamicNeighborBasedReservoirSampling;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import cs.utils.Utils;
import cs.utils.graphdb.GraphDBUtils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class QbSampling {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    StringEncoder stringEncoder;
    String typePredicate;
    NodeEncoder nodeEncoder;
    Integer maxEntityThreshold;
    Map<Integer, EntityData> entityDataMapContainer;
    Map<Integer, Integer> classEntityCount;
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    Map<Integer, Integer> reservoirCapacityPerClass;
    Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes;
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Integer> propCount;
    Map<Integer, Integer> sampledPropCount;

    public QbSampling(int expNoOfClasses, int expNoOfInstances, String typePredicate, Integer entitySamplingThreshold) {
        this.expectedNumberOfClasses = expNoOfClasses;
        this.expNoOfInstances = expNoOfInstances;
        this.typePredicate = typePredicate;
        this.classEntityCount = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.sampledEntitiesPerClass = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.classToPropWithObjTypes = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.entityDataMapContainer = new HashMap((int)((double)expNoOfInstances / 0.75 + 1.0));
        this.propCount = new HashMap(13334);
        this.sampledPropCount = new HashMap(13334);
        this.stringEncoder = new StringEncoder();
        this.nodeEncoder = new NodeEncoder();
        this.maxEntityThreshold = entitySamplingThreshold;
        this.shapeTripletSupport = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
    }

    public void run() {
        System.out.println("Started EndpointSampling ...");
        this.getNumberOfInstancesOfEachClass();
        this.dynamicNeighborBasedReservoirSampling();
        this.entityConstraintsExtractionBatch();
        Utility.writeSupportToFile(this.stringEncoder, this.shapeTripletSupport, this.sampledEntitiesPerClass);
        this.extractSHACLShapes(true, Main.qseFromSpecificClasses);
        Utility.writeClassFrequencyInFile(this.classEntityCount, this.stringEncoder);
    }

    private void getNumberOfInstancesOfEachClass() {
        StopWatch watch = new StopWatch();
        watch.start();
        String query = FilesUtil.readQuery("query2").replace(":instantiationProperty", this.typePredicate);
        this.graphDBUtils.runSelectQuery(query).forEach((result) -> {
            String c = result.getValue("class").stringValue();
            int classCount = 0;
            if (result.getBinding("classCount").getValue().isLiteral()) {
                Literal literalClassCount = (Literal)result.getBinding("classCount").getValue();
                classCount = literalClassCount.intValue();
            }

            this.classEntityCount.put(this.stringEncoder.encode(c), classCount);
        });
        watch.stop();
        PrintStream var10000 = System.out;
        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
        var10000.println("Time Elapsed getNumberOfInstancesOfEachClass: " + var10001 + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        Utils.logTime("getNumberOfInstancesOfEachClass ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void dynamicNeighborBasedReservoirSampling() {
        System.out.println("invoked:dynamicNeighborBasedReservoirSampling()");
        StopWatch watch = new StopWatch();
        watch.start();
        String queryToGetWikiDataEntities = "PREFIX onto: <http://www.ontotext.com/>  CONSTRUCT from onto:explicit WHERE { ?s " + this.typePredicate + " ?o .} ";
        Random random = new Random(100L);
        AtomicInteger lineCounter = new AtomicInteger();
        this.reservoirCapacityPerClass = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        int minEntityThreshold = 1;
        int samplingPercentage = Main.entitySamplingTargetPercentage;
        DynamicNeighborBasedReservoirSampling drs = new DynamicNeighborBasedReservoirSampling(this.entityDataMapContainer, this.sampledEntitiesPerClass, this.reservoirCapacityPerClass, this.nodeEncoder, this.stringEncoder);

        try {
            this.graphDBUtils.runConstructQuery(queryToGetWikiDataEntities).forEach((line) -> {
                try {
                    Resource var10000 = line.getSubject();
                    String triple = "<" + var10000 + "> <" + line.getPredicate() + "> <" + line.getObject() + "> .";
                    Node[] nodes = NxParser.parseNodes(triple);
                    int objID = this.stringEncoder.encode(nodes[2].getLabel());
                    this.sampledEntitiesPerClass.putIfAbsent(objID, new ArrayList(this.maxEntityThreshold));
                    this.reservoirCapacityPerClass.putIfAbsent(objID, minEntityThreshold);
                    if (((List)this.sampledEntitiesPerClass.get(objID)).size() < (Integer)this.reservoirCapacityPerClass.get(objID)) {
                        drs.sample(nodes);
                    } else {
                        drs.replace(random.nextInt(lineCounter.get()), nodes);
                    }

                    this.classEntityCount.merge(objID, 1, Integer::sum);
                    drs.resizeReservoir((Integer)this.classEntityCount.get(objID), ((List)this.sampledEntitiesPerClass.get(objID)).size(), this.maxEntityThreshold, samplingPercentage, objID);
                    lineCounter.getAndIncrement();
                } catch (ParseException var10) {
                    var10.printStackTrace();
                }

            });
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass:dynamicNeighborBasedReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        Utils.logSamplingStats("dynamicNeighborBasedReservoirSampling", samplingPercentage, minEntityThreshold, this.maxEntityThreshold, this.entityDataMapContainer.size());
    }

    private void entityConstraintsExtractionBatch() {
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println("Started secondPhaseBatch()");
        HashMap<Set<String>, List<String>> typesToEntities = new HashMap();
        Iterator var3 = this.entityDataMapContainer.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Integer, EntityData> entry = (Map.Entry)var3.next();
            Integer entityID = (Integer)entry.getKey();
            EntityData entityData = (EntityData)entry.getValue();
            Set<String> entityTypes = new HashSet();
            Iterator var8 = entityData.getClassTypes().iterator();

            while(var8.hasNext()) {
                Integer entityTypeID = (Integer)var8.next();
                entityTypes.add(this.stringEncoder.decode(entityTypeID));
            }

            typesToEntities.computeIfAbsent(entityTypes, (k) -> {
                return new ArrayList();
            });
            List<String> current = (List)typesToEntities.get(entityTypes);
            current.add(this.nodeEncoder.decode(entityID).toString());
            typesToEntities.put(entityTypes, current);
        }

        typesToEntities.forEach((types, entities) -> {
            if (entities.size() > 2000) {
                List<List<String>> subEntities = Lists.partition(entities, 2000);
                subEntities.forEach((listOfSubEntities) -> {
                    String batchQuery = Utility.buildBatchQuery(types, listOfSubEntities, this.typePredicate);
                    this.iterateOverEntityTriples(types, batchQuery);
                });
            } else {
                String batchQuery = Utility.buildBatchQuery(types, entities, this.typePredicate);
                this.iterateOverEntityTriples(types, batchQuery);
            }

        });
        watch.stop();
        Utils.logTime("secondPhaseBatch:cs.qse.endpoint.EndpointSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void iterateOverEntityTriples(Set<String> types, String batchQuery) {
        Iterator var3 = this.graphDBUtils.evaluateSelectQuery(batchQuery).iterator();

        while(var3.hasNext()) {
            BindingSet row = (BindingSet)var3.next();
            String entity = row.getValue("entity").stringValue();
            Integer entityID = this.nodeEncoder.encode(Utils.IriToNode(entity));
            String prop = row.getValue("p").stringValue();
            int propID = this.stringEncoder.encode(prop);
            String obj = row.getValue("o").stringValue();
            Node objNode = Utils.IriToNode(obj);
            String objType = Utility.extractObjectType(obj);
            Set<Integer> objTypesIDs = new HashSet(10);
            Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet(10);
            if (objType.equals("IRI")) {
                objTypesIDs = this.parseIriTypeObject(entityID, propID, objNode, (Set)objTypesIDs, prop2objTypeTuples);
            } else {
                this.parseLiteralTypeObject(entityID, propID, objType, (Set)objTypesIDs);
            }

            Iterator var14 = types.iterator();

            while(var14.hasNext()) {
                String entityTypeString = (String)var14.next();
                Integer entityTypeID = this.stringEncoder.encode(entityTypeString);
                Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.computeIfAbsent(entityTypeID, (k) -> {
                    return new HashMap();
                });
                Set<Integer> classObjTypes = (Set)propToObjTypes.computeIfAbsent(propID, (k) -> {
                    return new HashSet();
                });
                classObjTypes.addAll((Collection)objTypesIDs);
                propToObjTypes.put(propID, classObjTypes);
                this.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
                ((Set)objTypesIDs).forEach((objTypeID) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(entityTypeID, propID, objTypeID);
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        Integer support = ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport();
                        support = support + 1;
                        this.shapeTripletSupport.put(tuple3, new SupportConfidence(support));
                    } else {
                        this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                    }

                });
            }
        }

    }

    protected void extractSHACLShapes(Boolean performPruning, Boolean qseFromSpecificClasses) {
        System.out.println("Started extractSHACLShapes()");
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        ShapesExtractor se = new ShapesExtractor(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.typePredicate);
        if (qseFromSpecificClasses) {
            this.classToPropWithObjTypes = Utility.extractShapesForSpecificClasses(this.classToPropWithObjTypes, this.classEntityCount, this.stringEncoder);
        }

        se.constructDefaultShapes(this.classToPropWithObjTypes);
        if (performPruning) {
            StopWatch watchForPruning = new StopWatch();
            watchForPruning.start();
            ExperimentsUtil.getSupportConfRange().forEach((conf, supportRange) -> {
                supportRange.forEach((supp) -> {
                    StopWatch innerWatch = new StopWatch();
                    innerWatch.start();
                    se.constructPrunedShapes(this.classToPropWithObjTypes, conf, supp);
                    innerWatch.stop();
                    Utils.logTime("" + conf + "_" + supp, TimeUnit.MILLISECONDS.toSeconds(innerWatch.getTime()), TimeUnit.MILLISECONDS.toMinutes(innerWatch.getTime()));
                });
            });
            methodName = "extractSHACLShapes";
            watchForPruning.stop();
            Utils.logTime(methodName + "-Time.For.Pruning.Only", TimeUnit.MILLISECONDS.toSeconds(watchForPruning.getTime()), TimeUnit.MILLISECONDS.toMinutes(watchForPruning.getTime()));
        }

        ExperimentsUtil.prepareCsvForGroupedStackedBarChart(Constants.EXPERIMENTS_RESULT, Constants.EXPERIMENTS_RESULT_CUSTOM, true);
        watch.stop();
        Utils.logTime(methodName, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private Set<Integer> parseIriTypeObject(Integer entityID, int propID, Node objNode, Set<Integer> objTypesIDs, Set<Tuple2<Integer, Integer>> prop2objTypeTuples) {
        EntityData currEntityData = (EntityData)this.entityDataMapContainer.get(this.nodeEncoder.encode(objNode));
        if (currEntityData != null && currEntityData.getClassTypes().size() != 0) {
            objTypesIDs = currEntityData.getClassTypes();
            Iterator var9 = objTypesIDs.iterator();

            while(var9.hasNext()) {
                Integer objTypeID = (Integer)var9.next();
                prop2objTypeTuples.add(new Tuple2(propID, objTypeID));
            }

            this.addEntityToPropertyConstraints(prop2objTypeTuples, entityID);
        } else {
            int objID = this.stringEncoder.encode(Constants.OBJECT_UNDEFINED_TYPE);
            objTypesIDs.add(objID);
            prop2objTypeTuples = Collections.singleton(new Tuple2(propID, objID));
            this.addEntityToPropertyConstraints(prop2objTypeTuples, entityID);
        }

        return objTypesIDs;
    }

    private void parseLiteralTypeObject(Integer entityID, int propID, String objType, Set<Integer> objTypesIDs) {
        int objID = this.stringEncoder.encode(objType);
        objTypesIDs.add(objID);
        Set<Tuple2<Integer, Integer>> prop2objTypeTuples = Collections.singleton(new Tuple2(propID, objID));
        this.addEntityToPropertyConstraints(prop2objTypeTuples, entityID);
    }

    private void updateClassToPropWithObjTypesAndShapeTripletSupportMaps(EntityData entityData, int propID, Set<Integer> objTypesIDs) {
        Iterator var4 = entityData.getClassTypes().iterator();

        while(var4.hasNext()) {
            Integer entityTypeID = (Integer)var4.next();
            Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.computeIfAbsent(entityTypeID, (k) -> {
                return new HashMap();
            });
            Set<Integer> classObjTypes = (Set)propToObjTypes.computeIfAbsent(propID, (k) -> {
                return new HashSet();
            });
            classObjTypes.addAll(objTypesIDs);
            propToObjTypes.put(propID, classObjTypes);
            this.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
            objTypesIDs.forEach((objTypeID) -> {
                Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(entityTypeID, propID, objTypeID);
                if (this.shapeTripletSupport.containsKey(tuple3)) {
                    Integer support = ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport();
                    support = support + 1;
                    this.shapeTripletSupport.put(tuple3, new SupportConfidence(support));
                } else {
                    this.shapeTripletSupport.put(tuple3, new SupportConfidence(1));
                }

            });
        }

    }

    private void addEntityToPropertyConstraints(Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Integer subject) {
        EntityData currentEntityData = (EntityData)this.entityDataMapContainer.get(subject);
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

        this.entityDataMapContainer.put(subject, currentEntityData);
    }
}
