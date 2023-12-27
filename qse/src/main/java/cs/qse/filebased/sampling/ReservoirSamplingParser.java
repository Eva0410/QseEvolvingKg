//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased.sampling;

import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.common.ExperimentsUtil;
import cs.qse.common.ShapesExtractor;
import cs.qse.common.Utility;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.Parser;
import cs.qse.filebased.StatsComputer;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import cs.utils.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
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
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class ReservoirSamplingParser extends Parser {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    StringEncoder stringEncoder;
    StatsComputer statsComputer;
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

    public ReservoirSamplingParser(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate, Integer entitySamplingThreshold) {
        this.rdfFilePath = filePath;
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
    }

    public void run() {
        System.out.println("initiated:ReservoirSamplingParser");
        this.runParser();
    }

    private void runParser() {
        this.dynamicNbReservoirSampling();
        this.entityConstraintsExtraction();
        this.computeSupportConfidence();
        this.extractSHACLShapes(true, Main.qseFromSpecificClasses);
        Utility.writeClassFrequencyInFile(this.classEntityCount, this.stringEncoder);
    }

    public void standardReservoirSampling() {
        StopWatch watch = new StopWatch();
        watch.start();
        Random random = new Random(100L);
        AtomicInteger lineCounter = new AtomicInteger();
        StandardReservoirSampling srs = new StandardReservoirSampling(this.entityDataMapContainer, this.sampledEntitiesPerClass, this.nodeEncoder, this.stringEncoder);

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(this.typePredicate)) {
                        int objID = this.stringEncoder.encode(nodes[2].getLabel());
                        this.sampledEntitiesPerClass.putIfAbsent(objID, new ArrayList(this.maxEntityThreshold));
                        int numberOfSampledEntities = ((List)this.sampledEntitiesPerClass.get(objID)).size();
                        if (numberOfSampledEntities < this.maxEntityThreshold) {
                            srs.sample(nodes);
                        } else {
                            srs.replace(random.nextInt(lineCounter.get()), nodes);
                        }

                        this.classEntityCount.merge(objID, 1, Integer::sum);
                    }

                    lineCounter.getAndIncrement();
                } catch (ParseException var8) {
                    var8.printStackTrace();
                }

            });
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass:StandardReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void neighborBasedReservoirSampling() {
        StopWatch watch = new StopWatch();
        watch.start();
        Random random = new Random(100L);
        AtomicInteger lineCounter = new AtomicInteger();
        NeighborBasedReservoirSampling brs = new NeighborBasedReservoirSampling(this.entityDataMapContainer, this.sampledEntitiesPerClass, this.nodeEncoder, this.stringEncoder);

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(this.typePredicate)) {
                        int objID = this.stringEncoder.encode(nodes[2].getLabel());
                        this.sampledEntitiesPerClass.putIfAbsent(objID, new ArrayList(this.maxEntityThreshold));
                        int numberOfSampledEntities = ((List)this.sampledEntitiesPerClass.get(objID)).size();
                        if (numberOfSampledEntities < this.maxEntityThreshold) {
                            brs.sample(nodes);
                        } else {
                            brs.replace(random.nextInt(lineCounter.get()), nodes);
                        }

                        this.classEntityCount.merge(objID, 1, Integer::sum);
                    }

                    lineCounter.getAndIncrement();
                } catch (ParseException var8) {
                    var8.printStackTrace();
                }

            });
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass:neighborBasedReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void dynamicNbReservoirSampling() {
        System.out.println("invoked:dynamicNbReservoirSampling()");
        StopWatch watch = new StopWatch();
        watch.start();
        Random random = new Random(100L);
        AtomicInteger lineCounter = new AtomicInteger();
        this.reservoirCapacityPerClass = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        int minEntityThreshold = 1;
        int samplingPercentage = Main.entitySamplingTargetPercentage;
        DynamicNeighborBasedReservoirSampling drs = new DynamicNeighborBasedReservoirSampling(this.entityDataMapContainer, this.sampledEntitiesPerClass, this.reservoirCapacityPerClass, this.nodeEncoder, this.stringEncoder);

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(this.typePredicate)) {
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
                    } else {
                        this.propCount.merge(this.stringEncoder.encode(nodes[1].getLabel()), 1, Integer::sum);
                    }

                    lineCounter.getAndIncrement();
                } catch (ParseException var9) {
                    var9.printStackTrace();
                }

            });
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass:dynamicNbReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        Utils.logSamplingStats("dynamicNbReservoirSampling", samplingPercentage, minEntityThreshold, this.maxEntityThreshold, this.entityDataMapContainer.size());
    }

    private void prepareStatistics() {
        this.classEntityCount.forEach((classIRI, entityCount) -> {
            String log = "LOG:: " + classIRI + "," + this.stringEncoder.decode(classIRI) + "," + entityCount + "," + ((List)this.sampledEntitiesPerClass.get(classIRI)).size() + "," + this.reservoirCapacityPerClass.get(classIRI);
            Utils.writeLineToFile(log, Constants.THE_LOGS);
        });
    }

    public void entityConstraintsExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            Files.lines(Path.of(this.rdfFilePath)).filter((line) -> {
                return !line.contains(this.typePredicate);
            }).forEach((line) -> {
                try {
                    Set<Integer> objTypes = new HashSet(10);
                    Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet(10);
                    Node[] nodes = NxParser.parseNodes(line);
                    if (this.nodeEncoder.isNodeExists(nodes[0])) {
                        int subjID = this.nodeEncoder.getEncodedNode(nodes[0]);
                        if (this.entityDataMapContainer.get(subjID) != null) {
                            String objectType = this.extractObjectType(nodes[2].toString());
                            int propID = this.stringEncoder.encode(nodes[1].getLabel());
                            EntityData entityData;
                            Iterator var9;
                            Integer entityClass;
                            if (!objectType.equals("IRI")) {
                                int objID = this.stringEncoder.encode(objectType);
                                ((Set)objTypes).add(objID);
                                Set<Tuple2<Integer, Integer>> prop2objTypeTuplesx = Collections.singleton(new Tuple2(propID, objID));
                                this.addEntityToPropertyConstraints(prop2objTypeTuplesx, subjID);
                            } else {
                                entityData = (EntityData)this.entityDataMapContainer.get(this.nodeEncoder.encode(nodes[2]));
                                if (entityData != null) {
                                    objTypes = entityData.getClassTypes();
                                    var9 = ((Set)objTypes).iterator();

                                    while(var9.hasNext()) {
                                        entityClass = (Integer)var9.next();
                                        prop2objTypeTuples.add(new Tuple2(propID, entityClass));
                                    }

                                    this.addEntityToPropertyConstraints(prop2objTypeTuples, subjID);
                                }
                            }

                            entityData = (EntityData)this.entityDataMapContainer.get(subjID);
                            Object classObjTypes;
                            if (entityData != null) {
                                for(var9 = entityData.getClassTypes().iterator(); var9.hasNext(); ((Set)classObjTypes).addAll((Collection)objTypes)) {
                                    entityClass = (Integer)var9.next();
                                    Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.get(entityClass);
                                    if (propToObjTypes == null) {
                                        propToObjTypes = new HashMap();
                                        this.classToPropWithObjTypes.put(entityClass, propToObjTypes);
                                    }

                                    classObjTypes = (Set)((Map)propToObjTypes).get(propID);
                                    if (classObjTypes == null) {
                                        classObjTypes = new HashSet();
                                        ((Map)propToObjTypes).put(propID, classObjTypes);
                                    }
                                }
                            }

                            this.sampledPropCount.merge(propID, 1, Integer::sum);
                        }
                    }
                } catch (ParseException var13) {
                    var13.printStackTrace();
                }

            });
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        watch.stop();
        Utils.logTime("secondPass:cs.qse.filebased.sampling.ReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
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

    public void computeSupportConfidence() {
        StopWatch watch = new StopWatch();
        watch.start();
        this.shapeTripletSupport = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.statsComputer = new StatsComputer();
        this.statsComputer.setShapeTripletSupport(this.shapeTripletSupport);
        this.statsComputer.setSampledEntityCount(this.sampledEntitiesPerClass);
        this.statsComputer.setSamplingOn(true);
        this.statsComputer.computeSupportConfidenceWithEncodedEntities(this.entityDataMapContainer, this.classEntityCount);
        watch.stop();
        Utils.logTime("computeSupportConfidence:cs.qse.filebased.sampling.ReservoirSampling", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void extractSHACLShapes(Boolean performPruning, Boolean qseFromSpecificClasses) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:cs.qse.filebased.sampling.ReservoirSampling: No Pruning";
        ShapesExtractor se = new ShapesExtractor(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.typePredicate);
        se.setPropWithClassesHavingMaxCountOne(this.statsComputer.getPropWithClassesHavingMaxCountOne());
        if (qseFromSpecificClasses) {
            this.classToPropWithObjTypes = Utility.extractShapesForSpecificClasses(this.classToPropWithObjTypes, this.classEntityCount, this.stringEncoder);
        }

        se.constructDefaultShapes(this.classToPropWithObjTypes);
        se.setPropCount(this.propCount);
        se.setSampledPropCount(this.sampledPropCount);
        se.setSampledEntitiesPerClass(this.sampledEntitiesPerClass);
        se.setSamplingOn(true);
        if (performPruning) {
            StopWatch watchForPruning = new StopWatch();
            watchForPruning.start();
            ExperimentsUtil.getSupportConfRange().forEach((conf, supportRange) -> {
                supportRange.forEach((supp) -> {
                    se.constructPrunedShapes(this.classToPropWithObjTypes, conf, supp);
                });
            });
            methodName = "extractSHACLShapes:cs.qse.filebased.sampling.ReservoirSampling";
            watchForPruning.stop();
            Utils.logTime(methodName + "-Time.For.Pruning.Only", TimeUnit.MILLISECONDS.toSeconds(watchForPruning.getTime()), TimeUnit.MILLISECONDS.toMinutes(watchForPruning.getTime()));
        }

        ExperimentsUtil.prepareCsvForGroupedStackedBarChart(Constants.EXPERIMENTS_RESULT, Constants.EXPERIMENTS_RESULT_CUSTOM, true);
        watch.stop();
        Utils.logTime(methodName, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void printSampledEntitiesLogs() {
        System.out.println("entityDataMapContainer.size(): " + NumberFormat.getInstance().format((long)this.entityDataMapContainer.size()));
        System.out.println("nodeEncoder.getTable().size(): " + NumberFormat.getInstance().format((long)this.nodeEncoder.getTable().size()));
        System.out.println("nodeEncoder.getReverseTable().size(): " + NumberFormat.getInstance().format((long)this.nodeEncoder.getReverseTable().size()));
        System.out.println("nodeEncoder.counter: " + NumberFormat.getInstance().format((long)this.nodeEncoder.counter));
    }
}
