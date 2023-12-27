//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.utils;

import com.google.common.collect.Sets;
import cs.Main;
import cs.qse.common.ExperimentsUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class PrecisionRecallComputer {
    Set<Value> psA;
    Set<Value> psB;
    Map<Value, Set<Value>> npsA;
    Map<Value, Set<Value>> npsB;
    String baseAddressA;
    String baseAddressB;
    String outputFilePath;
    List<String> columnsCSV;
    List<String> precisionRecallCSV = new ArrayList();
    List<String> logsWronglyPrunedShapes = new ArrayList();

    public PrecisionRecallComputer() {
        this.getBaseAddress();
        this.prepareCsvHeader();
        this.computePrecisionRecallForDefaultModels();
        this.computePrecisionRecallForPrunedModels();
        this.precisionRecallCSV.forEach((line) -> {
            String fileAddress = this.outputFilePath + Main.datasetName + "_PrecisionRecall.csv";
            Utils.writeLineToFile(line, fileAddress);
        });
    }

    private void getBaseAddress() {
        Path path = Paths.get(Main.datasetPath);
        this.outputFilePath = Main.outputFilePath;
        String var10001 = ConfigManager.getProperty("default_directory");
        this.baseAddressA = var10001 + FilenameUtils.removeExtension(path.getFileName().toString());
        var10001 = this.outputFilePath;
        this.baseAddressB = var10001 + FilenameUtils.removeExtension(path.getFileName().toString());
    }

    private void prepareCsvHeader() {
        String header = "File_A, File_B, Confidence, Support, NS, PS, NS_Samp, PS_Samp, Precision_NS, Recall_NS, Precision_PS, Recall_PS, MaxReservoirSize, TargetPercentage";
        this.precisionRecallCSV.add(header);
    }

    private void computePrecisionRecallForDefaultModels() {
        String fileA = this.baseAddressA + "_DEFAULT_SHACL.ttl";
        String fileB = this.baseAddressB + "_DEFAULT_SHACL.ttl";
        this.columnsCSV = new ArrayList();
        this.columnsCSV.add(fileA);
        this.columnsCSV.add(fileB);
        this.columnsCSV.add("-");
        this.columnsCSV.add("-");
        this.processNsAndPs(fileA, fileB);
        this.computePrecisionRecall();
        this.columnsCSV.add(String.valueOf(Main.entitySamplingThreshold));
        this.columnsCSV.add(String.valueOf(Main.entitySamplingTargetPercentage));
        this.precisionRecallCSV.add(StringUtils.join(this.columnsCSV, ","));
    }

    private void computePrecisionRecallForPrunedModels() {
        Iterator var1 = ExperimentsUtil.getSupportConfRange().entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Double, List<Integer>> entry = (Map.Entry)var1.next();
            Double conf = (Double)entry.getKey();
            List<Integer> supportRange = (List)entry.getValue();
            Iterator var5 = supportRange.iterator();

            while(var5.hasNext()) {
                Integer supp = (Integer)var5.next();
                String fileA = this.baseAddressA + "_CUSTOM_" + conf + "_" + supp + "_SHACL.ttl";
                String fileB = this.baseAddressB + "_CUSTOM_" + conf + "_" + supp + "_SHACL.ttl";
                this.columnsCSV = new ArrayList();
                this.columnsCSV.add(fileA);
                this.columnsCSV.add(fileB);
                this.columnsCSV.add(String.valueOf(conf));
                this.columnsCSV.add(String.valueOf(supp));
                this.processNsAndPs(fileA, fileB);
                this.computePrecisionRecall();
                this.columnsCSV.add(String.valueOf(Main.entitySamplingThreshold));
                this.columnsCSV.add(String.valueOf(Main.entitySamplingTargetPercentage));
                this.precisionRecallCSV.add(StringUtils.join(this.columnsCSV, ","));
            }
        }

    }

    private void processNsAndPs(String fileA, String fileB) {
        Model modelA = createModel(fileA);
        Model modelB = createModel(fileB);

        npsA = getNodePropShapes(modelA);
        npsB = getNodePropShapes(modelB);

        psA = new HashSet<>();
        psB = new HashSet<>();

        npsA.values().forEach(psA::addAll);
        npsB.values().forEach(psB::addAll);

        columnsCSV.add(String.valueOf(npsA.keySet().size()));
        columnsCSV.add(String.valueOf(psA.size()));

        columnsCSV.add(String.valueOf(npsB.keySet().size()));
        columnsCSV.add(String.valueOf(psB.size()));
    }

    private void computePrecisionRecall() {
        Set<Value> commonNs = Sets.intersection(this.npsA.keySet(), this.npsB.keySet());
        Set<Value> commonPs = Sets.intersection(this.psA, this.psB);
        double precision_ns = this.divide(commonNs.size(), this.npsB.keySet().size());
        double recall_ns = this.divide(commonNs.size(), this.npsA.keySet().size());
        double precision_ps = this.divide(commonPs.size(), this.psB.size());
        double recall_ps = this.divide(commonPs.size(), this.psA.size());
        DecimalFormat df = new DecimalFormat("0.00");
        this.columnsCSV.add(df.format(precision_ns));
        this.columnsCSV.add(df.format(recall_ns));
        this.columnsCSV.add(df.format(precision_ps));
        this.columnsCSV.add(df.format(recall_ps));
    }

    private void computeStatisticsOfWronglyPrunedShapes() {
        Set<Value> diffNs = Sets.difference(this.npsA.keySet(), this.npsB.keySet());
        Set<Value> diffPs = Sets.difference(this.psA, this.psB);
    }

    private Model createModel(String file) {
        Model model = null;

        try {
            model = Rio.parse(new FileInputStream(file), "", RDFFormat.TURTLE, new Resource[0]);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return model;
    }

    private Map<Value, Set<Value>> getNodePropShapes(Model model) {
        Map<Value, Set<Value>> nodeToPropertyShapes = new HashMap<>();
        Repository db = new SailRepository(new MemoryStore());
        db.init();
        try (RepositoryConnection conn = db.getConnection()) {
            conn.add(model);
            conn.setNamespace("sh", SHACL.NAMESPACE);
            conn.setNamespace("shape", Constants.SHAPES_NAMESPACE);
            TupleQuery queryNs = conn.prepareTupleQuery(readQuery("query_node_shapes"));
            List<Value> queryNsOutput = executeQuery(queryNs, "nodeShape");
            queryNsOutput.forEach(value -> {
                nodeToPropertyShapes.putIfAbsent(value, new HashSet<>());
                TupleQuery queryPs = conn.prepareTupleQuery(readQuery("query_property_shapes").replace("NODE_SHAPE", value.stringValue()));
                List<Value> queryPsOutput = executeQuery(queryPs, "propertyShape");
                nodeToPropertyShapes.get(value).addAll(queryPsOutput);
            });
            return nodeToPropertyShapes;
        } finally {
            db.shutDown();
        }
    }

    private List<Value> executeQuery(TupleQuery query, String bindingName) {
        List<Value> output = new ArrayList();

        try {
            TupleQueryResult result = query.evaluate();

            try {
                while(result.hasNext()) {
                    BindingSet solution = (BindingSet)result.next();
                    output.add(solution.getValue(bindingName));
                }
            } catch (Throwable var8) {
                if (result != null) {
                    try {
                        result.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (result != null) {
                result.close();
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return output;
    }

    private String readQuery(String query) {
        String q = null;

        try {
            String queriesDirectory = ConfigManager.getProperty("resources_path") + "/stats/";
            q = new String(Files.readAllBytes(Paths.get(queriesDirectory + query + ".txt")));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return q;
    }

    private double divide(int nominator, int denominator) {
        return (double)nominator / (double)denominator;
    }
}
