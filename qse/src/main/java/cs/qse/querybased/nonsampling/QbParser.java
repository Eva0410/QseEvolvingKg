//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.nonsampling;

import cs.Main;
import cs.qse.common.ExperimentsUtil;
import cs.qse.common.ShapesExtractor;
import cs.qse.common.Utility;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple3;
import cs.utils.Utils;
import cs.utils.graphdb.GraphDBUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.BindingSet;
import org.jetbrains.annotations.NotNull;

public class QbParser {
    private final GraphDBUtils graphDBUtils;
    HashMap<Integer, Integer> classEntityCount;
    Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes;
    HashMap<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Set<Integer> classes;
    StringEncoder stringEncoder;
    String instantiationProperty;
    Boolean qseFromSpecificClasses;
    long globalComputeSupportMethodTime = 0L;
    public ShapesExtractor shapesExtractor;

    public QbParser(String typeProperty) {
        this.graphDBUtils = new GraphDBUtils();
        int expectedNumberOfClasses = Integer.parseInt(ConfigManager.getProperty("expected_number_classes"));
        this.classEntityCount = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.classToPropWithObjTypes = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.shapeTripletSupport = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.stringEncoder = new StringEncoder();
        this.instantiationProperty = typeProperty;
        this.qseFromSpecificClasses = Main.qseFromSpecificClasses;
    }

    public QbParser(int expectedNumberOfClasses, String typePredicate, String graphdbUrl, String graphdbRepository) {
        this.graphDBUtils = new GraphDBUtils(graphdbUrl, graphdbRepository);
        this.classEntityCount = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.classToPropWithObjTypes = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.shapeTripletSupport = new HashMap((int)((double)expectedNumberOfClasses / 0.75 + 1.0));
        this.stringEncoder = new StringEncoder();
        this.instantiationProperty = typePredicate;
        this.qseFromSpecificClasses = false;
    }

    public void run() {
        this.runParser();
    }

    private void runParser() {
        this.getNumberOfInstancesOfEachClass();
        this.getDistinctClasses();
        this.getShapesInfoAndComputeSupport();
        System.out.println("globalComputeSupportMethodTime: " + this.globalComputeSupportMethodTime);
        this.extractSHACLShapes(true);
        this.writeSupportToFile();
        Utility.writeClassFrequencyInFile(this.classEntityCount, this.stringEncoder);
        PrintStream var10000 = System.out;
        int var10001 = this.classToPropWithObjTypes.size();
        var10000.println("Size: classToPropWithObjTypes :: " + var10001 + " , Size: shapeTripletSupport :: " + this.shapeTripletSupport.size());
    }

    public void getNumberOfInstancesOfEachClass() {
        StopWatch watch = new StopWatch();
        watch.start();
        if (this.qseFromSpecificClasses) {
            List<String> classes = Utility.getListOfClasses();
            Iterator var3 = classes.iterator();

            while(var3.hasNext()) {
                String classIri = (String)var3.next();
                this.graphDBUtils.runSelectQuery(this.setProperty(FilesUtil.readQuery("query2_")).replace(":Class", "<" + classIri + "> ")).forEach((result) -> {
                    int classCount = 0;
                    if (result.getBinding("classCount").getValue().isLiteral()) {
                        Literal literalClassCount = (Literal)result.getBinding("classCount").getValue();
                        classCount = literalClassCount.intValue();
                    }

                    this.classEntityCount.put(this.stringEncoder.encode(classIri), classCount);
                });
            }
        } else {
            this.graphDBUtils.runSelectQuery(this.setProperty(FilesUtil.readQuery("query2"))).forEach((result) -> {
                String c = result.getValue("class").stringValue();
                int classCount = 0;
                if (result.getBinding("classCount").getValue().isLiteral()) {
                    Literal literalClassCount = (Literal)result.getBinding("classCount").getValue();
                    classCount = literalClassCount.intValue();
                }

                this.classEntityCount.put(this.stringEncoder.encode(c), classCount);
            });
        }

        watch.stop();
        PrintStream var10000 = System.out;
        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
        var10000.println("Time Elapsed getNumberOfInstancesOfEachClass: " + var10001 + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        Utils.logTime("getNumberOfInstancesOfEachClass ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void getDistinctClasses() {
        this.classes = this.classEntityCount.keySet();
    }

    public void setClasses(Set<Integer> values) {
        this.classes = values;
    }

    public void getShapesInfoAndComputeSupport() {
        StopWatch watch = new StopWatch();
        watch.start();
        for (Integer classIri : classes) {
            String queryToGetProperties = FilesUtil.readQuery("query4").replace(":Class", " <" + stringEncoder.decode(classIri) + "> ");
            queryToGetProperties = setProperty(queryToGetProperties);
            HashSet<String> props = getPropertiesOfClass(queryToGetProperties);

            HashMap<Integer, Set<Integer>> propToObjTypes = new HashMap<>();

            for (String property : props) {
                HashSet<Integer> objectTypes = new HashSet<>();
                String queryToVerifyLiteralObjectType = buildQuery(classIri, property, "query5");
                queryToVerifyLiteralObjectType = setProperty(queryToVerifyLiteralObjectType);
                //Literal Type Object
                if (graphDBUtils.runAskQuery(queryToVerifyLiteralObjectType)) {
                    String queryToGetDataTypeOfLiteralObject = buildQuery(classIri, property, "query6");
                    List<BindingSet> results = graphDBUtils.runSelectQuery(queryToGetDataTypeOfLiteralObject);
                    if (results != null) {
                        results.forEach(row -> {
                            if (row.getValue("objDataType") != null) {
                                String objectType = row.getValue("objDataType").stringValue();
                                objectTypes.add(stringEncoder.encode(objectType));
                                String queryToComputeSupportForLiteralTypeObjects = buildQuery(classIri, property, objectType, "query8");
                                queryToComputeSupportForLiteralTypeObjects = setProperty(queryToComputeSupportForLiteralTypeObjects);
                                computeSupport(classIri, property, objectType, queryToComputeSupportForLiteralTypeObjects);
                            } else {
                                System.out.println("WARNING:: it's null for literal type object .. " + classIri + " - " + property);
                            }
                        });
                    }

                }
                //Non-Literal Type Object
                else {
                    String queryToGetDataTypeOfNonLiteralObjects = buildQuery(classIri, property, "query7");
                    queryToGetDataTypeOfNonLiteralObjects = setProperty(queryToGetDataTypeOfNonLiteralObjects);
                    List<BindingSet> results = graphDBUtils.runSelectQuery(queryToGetDataTypeOfNonLiteralObjects);
                    if (results != null) {
                        results.forEach(row -> {
                            if (row.getValue("objDataType") != null) {
                                String objectType = row.getValue("objDataType").stringValue();
                                objectTypes.add(stringEncoder.encode(objectType));
                                String queryToComputeSupportForNonLiteralTypeObjects = buildQuery(classIri, property, objectType, "query9");
                                queryToComputeSupportForNonLiteralTypeObjects = setProperty(queryToComputeSupportForNonLiteralTypeObjects);
                                computeSupport(classIri, property, objectType, queryToComputeSupportForNonLiteralTypeObjects);
                            } else {
                                System.out.println("WARNING:: it's null for Non-Literal type object .. " + classIri + " - " + property);
                            }
                        });
                    }

                    String queryToGetUndefinedNonLiteralObjects = buildQuery(classIri, property, "query7_");
                    queryToGetUndefinedNonLiteralObjects = setProperty(queryToGetUndefinedNonLiteralObjects);
                    List<BindingSet> resultsUndefinedTriples = graphDBUtils.runSelectQuery(queryToGetUndefinedNonLiteralObjects);
                    if (resultsUndefinedTriples != null && resultsUndefinedTriples.size() > 0) {
                        String undefinedObjType = "http://shaclshapes.org/undefined";
                        objectTypes.add(stringEncoder.encode(undefinedObjType));
                        String queryToComputeSupportForNonLiteralTypeObjects = buildQuery(classIri, property, undefinedObjType, "query9_");
                        queryToComputeSupportForNonLiteralTypeObjects = setProperty(queryToComputeSupportForNonLiteralTypeObjects);
                        computeSupport(classIri, property, undefinedObjType, queryToComputeSupportForNonLiteralTypeObjects);
                    }
                }
                propToObjTypes.put(stringEncoder.encode(property), objectTypes);
            }
            classToPropWithObjTypes.put(classIri, propToObjTypes);
            System.out.println(".. globalComputeSupportMethodTime: " + globalComputeSupportMethodTime);

        }
        watch.stop();
        System.out.println("Time Elapsed getShapesInfoAndComputeSupport: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        Utils.logTime("getShapesInfoAndComputeSupport ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void computeSupport(Integer classIri, String property, String objectType, String supportQuery) {
        StopWatch watcher = new StopWatch();
        watcher.start();
        Iterator var6 = this.graphDBUtils.runSelectQuery(supportQuery).iterator();

        while(var6.hasNext()) {
            BindingSet countRow = (BindingSet)var6.next();
            if (countRow.getBinding("count").getValue().isLiteral()) {
                Literal literalCount = (Literal)countRow.getBinding("count").getValue();
                int support = literalCount.intValue();
                int clasEntityCount = (Integer)this.classEntityCount.get(classIri);
                Double confidence = (double)support / (double)clasEntityCount;
                this.shapeTripletSupport.put(new Tuple3(classIri, this.stringEncoder.encode(property), this.stringEncoder.encode(objectType)), new SupportConfidence(support, confidence));
            }
        }

        watcher.stop();
        long time = watcher.getTime();
        this.globalComputeSupportMethodTime += time;
    }

    public void extractSHACLShapes(Boolean performPruning) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        ShapesExtractor se = new ShapesExtractor(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.instantiationProperty);
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

    public String extractSHACLShapes() {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:No Pruning";
        this.shapesExtractor = new ShapesExtractor(this.stringEncoder, this.shapeTripletSupport, this.classEntityCount, this.instantiationProperty);
        this.shapesExtractor.constructDefaultShapes(this.classToPropWithObjTypes);
        ExperimentsUtil.prepareCsvForGroupedStackedBarChart(Constants.EXPERIMENTS_RESULT, Constants.EXPERIMENTS_RESULT_CUSTOM, true);
        watch.stop();
        Utils.logTime(methodName, TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        return this.shapesExtractor.getOutputFileAddress();
    }

    public String extractSHACLShapesWithPruning(Double conf, Integer supp) {
        StopWatch watch = new StopWatch();
        watch.start();
        String methodName = "extractSHACLShapes:WithPruning";
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

    public void writeSupportToFile() {
        try {
            FileWriter fileWriter = new FileWriter(new File(Constants.TEMP_DATASET_FILE), true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Iterator var3 = this.shapeTripletSupport.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var3.next();
                Tuple3<Integer, Integer, Integer> tupl3 = (Tuple3)entry.getKey();
                Integer count = ((SupportConfidence)entry.getValue()).getSupport();
                String log = this.stringEncoder.decode((Integer)tupl3._1) + "|" + this.stringEncoder.decode((Integer)tupl3._2) + "|" + this.stringEncoder.decode((Integer)tupl3._3) + "|" + count + "|" + this.classEntityCount.get(tupl3._1);
                printWriter.println(log);
            }

            printWriter.close();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

    }

    @NotNull
    private String buildQuery(Integer classIri, String property, String queryFile) {
        String var10000 = FilesUtil.readQuery(queryFile);
        StringEncoder var10002 = this.stringEncoder;
        return var10000.replace(":Class", " <" + var10002.decode(classIri) + "> ").replace(":Prop", " <" + property + "> ");
    }

    @NotNull
    private String buildQuery(Integer classIri, String property, String objectType, String queryFile) {
        String var10000 = FilesUtil.readQuery(queryFile);
        StringEncoder var10002 = this.stringEncoder;
        return var10000.replace(":Class", " <" + var10002.decode(classIri) + "> ").replace(":Prop", " <" + property + "> ").replace(":ObjectType", " <" + objectType + "> ");
    }

    @NotNull
    private HashSet<String> getPropertiesOfClass(String query) {
        HashSet<String> props = new HashSet();
        this.graphDBUtils.runSelectQuery(query).forEach((result) -> {
            String propIri = result.getValue("prop").stringValue();
            props.add(propIri);
        });
        return props;
    }

    private String setProperty(String query) {
        return query.replace(":instantiationProperty", this.instantiationProperty);
    }

    public StringEncoder getStringEncoder() {
        return this.stringEncoder;
    }

    public GraphDBUtils getGraphDBUtils() {
        return this.graphDBUtils;
    }

    public HashMap<Integer, Integer> getClassEntityCount() {
        return this.classEntityCount;
    }

    public Map<Integer, Map<Integer, Set<Integer>>> getClassToPropWithObjTypes() {
        return this.classToPropWithObjTypes;
    }

    public HashMap<Tuple3<Integer, Integer, Integer>, SupportConfidence> getShapeTripletSupport() {
        return this.shapeTripletSupport;
    }

    public Set<Integer> getClasses() {
        return this.classes;
    }

    public String getInstantiationProperty() {
        return this.instantiationProperty;
    }

    public Boolean getQseFromSpecificClasses() {
        return this.qseFromSpecificClasses;
    }

    public long getGlobalComputeSupportMethodTime() {
        return this.globalComputeSupportMethodTime;
    }
}
