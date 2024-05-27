//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.filebased;

import cs.Main;
//import cs.mg.MembershipGraph;
import cs.qse.common.*;
import cs.qse.common.encoders.StringEncoder;
import cs.utils.Constants;
import cs.utils.Tuple2;
import cs.utils.Tuple3;
import cs.utils.Utils;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
//Unfortunately WebApp does not work with current version of QSE. Therefore old version is saved here

public class Parser_Old {
    String rdfFilePath;
    Integer expectedNumberOfClasses;
    Integer expNoOfInstances;
    StringEncoder stringEncoder;
    StatsComputer statsComputer;
    String typePredicate;
    public Map<Node, EntityData> entityDataHashMap;
    public Map<Integer, Integer> classEntityCount;
    public Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes;
    public Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
//    public MembershipGraph mg;
    public ShapesExtractor_Old shapesExtractor;

    public Parser_Old() {
    }

    public Parser_Old(String filePath, int expNoOfClasses, int expNoOfInstances, String typePredicate) {
        this.rdfFilePath = filePath;
        this.expectedNumberOfClasses = expNoOfClasses;
        this.expNoOfInstances = expNoOfInstances;
        this.typePredicate = typePredicate;
        this.classEntityCount = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.classToPropWithObjTypes = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.entityDataHashMap = new HashMap((int)((double)expNoOfInstances / 0.75 + 1.0));
        this.stringEncoder = new StringEncoder();
    }

    public void run() {
        this.entityExtraction();
        this.entityConstraintsExtraction();
        this.computeSupportConfidence();
        this.extractSHACLShapes(false, Main.qseFromSpecificClasses);
        Utility.writeClassFrequencyInFile(this.classEntityCount, this.stringEncoder);
        System.out.println("STATS: \n\tNo. of Classes: " + this.classEntityCount.size());
//        this.createMembershipGraph();
    }

    public void entityExtraction() {
        System.out.println("invoked::firstPass()");
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Node[] nodes = NxParser.parseNodes(line);
                    if (nodes[1].toString().equals(this.typePredicate)) {
                        int objID = this.stringEncoder.encode(nodes[2].getLabel());
                        EntityData entityData = (EntityData)this.entityDataHashMap.get(nodes[0]);
                        if (entityData == null) {
                            entityData = new EntityData();
                        }

                        entityData.getClassTypes().add(objID);
                        this.entityDataHashMap.put(nodes[0], entityData);
                        this.classEntityCount.merge(objID, 1, Integer::sum);
                    }
                } catch (ParseException var5) {
                    var5.printStackTrace();
                }

            });
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        watch.stop();
        Utils.logTime("firstPass", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void entityConstraintsExtraction() {
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            Files.lines(Path.of(this.rdfFilePath)).forEach((line) -> {
                try {
                    Set<Integer> objTypesIDs = new HashSet(10);
                    Set<Tuple2<Integer, Integer>> prop2objTypeTuples = new HashSet(10);
                    Node[] nodes = NxParser.parseNodes(line);
                    Node entityNode = nodes[0];
                    String objectType = this.extractObjectType(nodes[2].toString());
                    int propID = this.stringEncoder.encode(nodes[1].getLabel());
                    if (objectType.equals("IRI")) {
                        objTypesIDs = this.parseIriTypeObject((Set)objTypesIDs, prop2objTypeTuples, nodes, entityNode, propID);
                    } else {
                        this.parseLiteralTypeObject((Set)objTypesIDs, entityNode, objectType, propID);
                    }

                    this.updateClassToPropWithObjTypesMap((Set)objTypesIDs, entityNode, propID);
                } catch (ParseException var8) {
                    var8.printStackTrace();
                }

            });
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        watch.stop();
        Utils.logTime("secondPhase", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void computeSupportConfidence() {
        StopWatch watch = new StopWatch();
        watch.start();
        this.shapeTripletSupport = new HashMap((int)((double)this.expectedNumberOfClasses / 0.75 + 1.0));
        this.statsComputer = new StatsComputer();
        this.statsComputer.setShapeTripletSupport(this.shapeTripletSupport);
        this.statsComputer.computeSupportConfidence(this.entityDataHashMap, this.classEntityCount);
        watch.stop();
        Utils.logTime("computeSupportConfidence", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void extractSHACLShapes(Boolean performPruning, Boolean qseFromSpecificClasses) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        this.shapesExtractor = new ShapesExtractor_Old(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.typePredicate);
        this.shapesExtractor.setPropWithClassesHavingMaxCountOne(this.statsComputer.getPropWithClassesHavingMaxCountOne());
        if (qseFromSpecificClasses) {
            this.classToPropWithObjTypes = Utility.extractShapesForSpecificClasses(this.classToPropWithObjTypes, this.classEntityCount, this.stringEncoder);
        }

        this.shapesExtractor.constructDefaultShapes(this.classToPropWithObjTypes);
        if (performPruning) {
            StopWatch watchForPruning = new StopWatch();
            watchForPruning.start();
            ExperimentsUtil.getSupportConfRange().forEach((conf, supportRange) -> {
                supportRange.forEach((supp) -> {
                    StopWatch innerWatch = new StopWatch();
                    innerWatch.start();
                    this.shapesExtractor.constructPrunedShapes(this.classToPropWithObjTypes, conf, supp);
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

    public String extractSHACLShapes(Boolean qseFromSpecificClasses, List<String> classes) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        this.shapesExtractor = new ShapesExtractor_Old(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.typePredicate);
        this.shapesExtractor.setPropWithClassesHavingMaxCountOne(this.statsComputer.getPropWithClassesHavingMaxCountOne());
        if (qseFromSpecificClasses) {
            this.classToPropWithObjTypes = Utility.extractShapesForSpecificClasses(this.classToPropWithObjTypes, this.classEntityCount, this.stringEncoder, classes);
        }

        this.shapesExtractor.constructDefaultShapes(this.classToPropWithObjTypes);
        ExperimentsUtil.prepareCsvForGroupedStackedBarChart(Constants.EXPERIMENTS_RESULT, Constants.EXPERIMENTS_RESULT_CUSTOM, true);
        watch.stop();
        Utils.logTime(methodName, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        return this.shapesExtractor.getOutputFileAddress();
    }

    public String extractSHACLShapesWithPruning(Boolean qseFromSpecificClasses, Double conf, Integer supp, List<String> classes) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:WithPruning";
        if (qseFromSpecificClasses) {
            this.classToPropWithObjTypes = Utility.extractShapesForSpecificClasses(this.classToPropWithObjTypes, this.classEntityCount, this.stringEncoder, classes);
        }

        StopWatch watchForPruning = new StopWatch();
        watchForPruning.start();
        this.shapesExtractor.constructPrunedShapes(this.classToPropWithObjTypes, conf, supp);
        watchForPruning.stop();
        Utils.logTime("" + conf + "_" + supp + " " + methodName + "-Time.For.Pruning.Only", TimeUnit.MILLISECONDS.toSeconds(watchForPruning.getTime()), TimeUnit.MILLISECONDS.toMinutes(watchForPruning.getTime()));
        ExperimentsUtil.prepareCsvForGroupedStackedBarChart(Constants.EXPERIMENTS_RESULT, Constants.EXPERIMENTS_RESULT_CUSTOM, true);
        watch.stop();
        Utils.logTime(methodName, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        return this.shapesExtractor.getOutputFileAddress();
    }

//    public void createMembershipGraph() {
//        StopWatch watch = new StopWatch();
//        watch.start();
//        this.mg = new MembershipGraph(this.stringEncoder, this.entityDataHashMap, this.classEntityCount);
//        this.mg.createMembershipSets();
//        this.mg.createMembershipGraph();
//        this.mg.visualizeMg();
//        System.out.println("Vertices: " + this.mg.getMembershipGraph().vertexSet().size());
//        System.out.println("Edges: " + this.mg.getMembershipGraph().edgeSet().size());
//        watch.stop();
//        PrintStream var10000 = System.out;
//        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
//        var10000.println("Time Elapsed MembershipGraphConstruction: " + var10001 + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
//    }

    public void computeGraphStats() {
    }

    private Set<Integer> parseIriTypeObject(Set<Integer> objTypesIDs, Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Node[] nodes, Node subject, int propID) {
        EntityData currEntityData = (EntityData)this.entityDataHashMap.get(nodes[2]);
        if (currEntityData != null && currEntityData.getClassTypes().size() != 0) {
            objTypesIDs = currEntityData.getClassTypes();
            Iterator var9 = objTypesIDs.iterator();

            while(var9.hasNext()) {
                Integer node = (Integer)var9.next();
                prop2objTypeTuples.add(new Tuple2(propID, node));
            }

            this.addEntityToPropertyConstraints(prop2objTypeTuples, subject);
        } else {
            int objID = this.stringEncoder.encode(Constants.OBJECT_UNDEFINED_TYPE);
            objTypesIDs.add(objID);
            prop2objTypeTuples = Collections.singleton(new Tuple2(propID, objID));
            this.addEntityToPropertyConstraints(prop2objTypeTuples, subject);
        }

        return objTypesIDs;
    }

    private void parseLiteralTypeObject(Set<Integer> objTypes, Node subject, String objectType, int propID) {
        int objID = this.stringEncoder.encode(objectType);
        objTypes.add(objID);
        Set<Tuple2<Integer, Integer>> prop2objTypeTuples = Collections.singleton(new Tuple2(propID, objID));
        this.addEntityToPropertyConstraints(prop2objTypeTuples, subject);
    }

    private void updateClassToPropWithObjTypesMap(Set<Integer> objTypesIDs, Node entityNode, int propID) {
        EntityData entityData = (EntityData)this.entityDataHashMap.get(entityNode);
        if (entityData != null) {
            Iterator var5 = entityData.getClassTypes().iterator();

            while(var5.hasNext()) {
                Integer entityTypeID = (Integer)var5.next();
                Map<Integer, Set<Integer>> propToObjTypes = (Map)this.classToPropWithObjTypes.computeIfAbsent(entityTypeID, (k) -> {
                    return new HashMap();
                });
                Set<Integer> classObjTypes = (Set)propToObjTypes.computeIfAbsent(propID, (k) -> {
                    return new HashSet();
                });
                classObjTypes.addAll(objTypesIDs);
                propToObjTypes.put(propID, classObjTypes);
                this.classToPropWithObjTypes.put(entityTypeID, propToObjTypes);
            }
        }

    }

    public void addEntityToPropertyConstraints(Set<Tuple2<Integer, Integer>> prop2objTypeTuples, Node subject) {
        EntityData currentEntityData = (EntityData)this.entityDataHashMap.get(subject);
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

        this.entityDataHashMap.put(subject, currentEntityData);
    }

    public String extractObjectType(String literalIri) {
        Literal theLiteral = new Literal(literalIri, true);
        String type = null;
        if (theLiteral.getDatatype() != null) {
            type = theLiteral.getDatatype().toString();
        } else if (theLiteral.getLanguageTag() != null) {
            type = "<" + RDF.LANGSTRING + ">";
        } else if (Utils.isValidIRI(literalIri)) {
            if (SimpleValueFactory.getInstance().createIRI(literalIri).isIRI()) {
                type = "IRI";
            }
        } else {
            type = "<" + XSD.STRING + ">";
        }

        return type;
    }

    public void assignCardinalityConstraints() {
        StopWatch watch = new StopWatch();
        watch.start();
        MinCardinalityExperiment minCardinalityExperiment = new MinCardinalityExperiment(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount);
        minCardinalityExperiment.constructDefaultShapes(this.classToPropWithObjTypes);
        ExperimentsUtil.getMinCardinalitySupportConfRange().forEach((conf, supportRange) -> {
            supportRange.forEach((supp) -> {
                minCardinalityExperiment.constructPrunedShapes(this.classToPropWithObjTypes, conf, supp);
            });
        });
        watch.stop();
        Utils.logTime("assignCardinalityConstraints", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public String getRdfFilePath() {
        return this.rdfFilePath;
    }

    public Integer getExpectedNumberOfClasses() {
        return this.expectedNumberOfClasses;
    }

    public StringEncoder getStringEncoder() {
        return this.stringEncoder;
    }

    public StatsComputer getStatsComputer() {
        return this.statsComputer;
    }

    public String getTypePredicate() {
        return this.typePredicate;
    }

    public Map<Node, EntityData> getEntityDataHashMap() {
        return this.entityDataHashMap;
    }

    public Map<Integer, Integer> getClassEntityCount() {
        return this.classEntityCount;
    }

    public Map<Integer, Map<Integer, Set<Integer>>> getClassToPropWithObjTypes() {
        return this.classToPropWithObjTypes;
    }

    public Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> getShapeTripletSupport() {
        return this.shapeTripletSupport;
    }
}