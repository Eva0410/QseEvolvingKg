//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.sampling.parallel;

import com.google.common.collect.Lists;
import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.common.ExperimentsUtil;
import cs.qse.common.ShapesExtractor;
import cs.qse.common.Utility;
import cs.qse.common.encoders.ConcurrentStringEncoder;
import cs.qse.common.encoders.NodeEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple3;
import cs.utils.Utils;
import cs.utils.graphdb.GraphDBUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class ParallelQbSampling {
    private final GraphDBUtils graphDBUtils = new GraphDBUtils();
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    ConcurrentStringEncoder encoder;
    String typePredicate;
    NodeEncoder nodeEncoder;
    Integer maxEntityThreshold;
    Integer numOfThreads;
    Map<Integer, EntityData> entityDataMapContainer;
    Map<Integer, Integer> classEntityCount;
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    Map<Integer, Integer> reservoirCapacityPerClass;
    Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes;
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Integer> propCount;
    Map<Integer, Integer> sampledPropCount;

    public ParallelQbSampling(int expNoOfClasses, int expNoOfInstances, String typePredicate, Integer entitySamplingThreshold, int numOfThreads) {
        this.expectedNumberOfClasses = expNoOfClasses;
        this.expNoOfInstances = expNoOfInstances;
        this.typePredicate = typePredicate;
        this.classEntityCount = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.sampledEntitiesPerClass = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.entityDataMapContainer = new HashMap((int)((double)expNoOfInstances / 0.75 + 1.0));
        this.encoder = new ConcurrentStringEncoder();
        this.nodeEncoder = new NodeEncoder();
        this.maxEntityThreshold = entitySamplingThreshold;
        this.numOfThreads = numOfThreads;
    }

    public void run() {
        System.out.println("Started ParallelQbSampling with " + this.numOfThreads + " threads.");
        this.getNumberOfInstancesOfEachClass();
        this.dynamicNeighborBasedReservoirSampling();
        this.collectEntityPropData();
        Utility.writeSupportToFile(this.encoder, this.shapeTripletSupport, this.sampledEntitiesPerClass);
        this.extractSHACLShapes(false);
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

            this.classEntityCount.put(this.encoder.encode(c), classCount);
        });
        watch.stop();
        Utils.logTime("getNumberOfInstancesOfEachClass ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void dynamicNeighborBasedReservoirSampling() {
        System.out.println("started dynamicNeighborBasedReservoirSampling()");
        StopWatch watch = new StopWatch();
        watch.start();
        String queryToGetWikiDataEntities = "PREFIX onto: <http://www.ontotext.com/>  CONSTRUCT from onto:explicit WHERE { ?s " + this.typePredicate + " ?o .} ";
        Random random = new Random(100L);
        AtomicInteger lineCounter = new AtomicInteger();
        this.reservoirCapacityPerClass = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        int minEntityThreshold = 1;
        int samplingPercentage = Main.entitySamplingTargetPercentage;
        NbResSamplingForQb drs = new NbResSamplingForQb(this.entityDataMapContainer, this.sampledEntitiesPerClass, this.reservoirCapacityPerClass, this.nodeEncoder, this.encoder);

        try {
            this.graphDBUtils.runConstructQuery(queryToGetWikiDataEntities).forEach((line) -> {
                try {
                    Resource var10000 = line.getSubject();
                    String triple = "<" + var10000 + "> <" + line.getPredicate() + "> <" + line.getObject() + "> .";
                    Node[] nodes = NxParser.parseNodes(triple);
                    int objID = this.encoder.encode(nodes[2].getLabel());
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

    private void collectEntityPropData() {
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println("Started collectEntityPropData(threads=" + numOfThreads + ")");

        try {
            List<Integer> entitiesList = new ArrayList<>(entityDataMapContainer.keySet());
            List<List<Integer>> entitiesPart = Lists.partition(entitiesList, entitiesList.size() / numOfThreads);

            //declare jobs
            List<Callable<SubEntityPropDataCollector>> jobs = new ArrayList<>();

            // create jobs
            int jobIndex = 1;
            for (List<Integer> part : entitiesPart) {
                int finalJobIndex = jobIndex;
                jobs.add(() -> {
                    SubEntityPropDataCollector subEntityPdc = new SubEntityPropDataCollector(expectedNumberOfClasses);
                    subEntityPdc.setFeatures(typePredicate, encoder, nodeEncoder);
                    subEntityPdc.job(finalJobIndex, part, entityDataMapContainer);
                    return subEntityPdc;
                });
                jobIndex++;
            }

            //execute jobs using invokeAll() method and collect results
            try {
                StopWatch sw = new StopWatch();
                sw.start();
                ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
                List<Future<SubEntityPropDataCollector>> subEntityPropDataCollectors = executor.invokeAll(jobs);
                executor.shutdownNow();
                sw.stop();
                Utils.logTime("Threads-Finished", TimeUnit.MILLISECONDS.toSeconds(sw.getTime()), TimeUnit.MILLISECONDS.toHours(sw.getTime()));
                int i = 0;
                for (Future<SubEntityPropDataCollector> subEntityPdc : subEntityPropDataCollectors) {
                    SubEntityPropDataCollector sEpDc = subEntityPdc.get();
                    if (i == 0) {
                        this.classToPropWithObjTypes = sEpDc.classToPropWithObjTypes;
                        this.shapeTripletSupport = sEpDc.shapeTripletSupport;
                        this.sampledPropCount = sEpDc.sampledPropCount;
                    } else {
                        mergeJobsOutput(sEpDc);
                    }
                    i++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        watch.stop();
        System.out.println("ParallelQbSampling.collectEntityPropData with : " + numOfThreads + " threads took " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " sec or " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()) + " min");
        Utils.logTime("cs.qse.querybased.sampling.parallel.ParallelQbSampling.collectEntityPropData with threads: " + numOfThreads + " ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    private void mergeJobsOutput(SubEntityPropDataCollector sEpDc) {
        sEpDc.classToPropWithObjTypes.forEach((c, localPwOt) -> {
            Map<Integer, Set<Integer>> globalPwOt = (Map)this.classToPropWithObjTypes.get(c);
            if (globalPwOt == null) {
                this.classToPropWithObjTypes.put(c, localPwOt);
            } else {
                localPwOt.forEach((p, localOt) -> {
                    Set<Integer> globalOt = (Set)globalPwOt.get(p);
                    if (globalOt == null) {
                        globalPwOt.put(p, localOt);
                    } else {
                        ((Set)globalPwOt.get(p)).addAll(localOt);
                    }

                });
                this.classToPropWithObjTypes.put(c, globalPwOt);
            }

        });
        sEpDc.shapeTripletSupport.forEach((tuple3, localSupp) -> {
            SupportConfidence globalSupp = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
            if (globalSupp == null) {
                this.shapeTripletSupport.put(tuple3, localSupp);
            } else {
                this.shapeTripletSupport.put(tuple3, new SupportConfidence(globalSupp.getSupport() + localSupp.getSupport()));
            }

        });
        sEpDc.sampledPropCount.forEach((p, localCount) -> {
            Integer globalCount = (Integer)this.sampledPropCount.get(p);
            if (globalCount == null) {
                this.sampledPropCount.put(p, localCount);
            } else {
                this.sampledPropCount.put(p, globalCount + localCount);
            }

        });
    }

    protected void extractSHACLShapes(Boolean performPruning) {
        System.out.println("Started extractSHACLShapes()");
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        ShapesExtractor se = new ShapesExtractor(this.encoder, this.shapeTripletSupport, this.classEntityCount, this.typePredicate);
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
}
