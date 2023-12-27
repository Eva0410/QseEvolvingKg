//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.validation;

import cs.utils.ConfigManager;
import cs.utils.FilesUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class QseSHACLValidator {
    public QseSHACLValidator() {
    }

    public QseSHACLValidator(Boolean flag) {
        System.out.println("Invoked::QseSHACLValidator");
        this.prepareValidation();
    }

    public void prepareValidation() {
        String dataFilePath = ConfigManager.getProperty("dataset_path");
        String inputFilesDirPath = ConfigManager.getProperty("validation_input_dir");

        assert inputFilesDirPath != null;

        File outputDir = new File(inputFilesDirPath + "/Output");
        if (!outputDir.exists()) {
            if (outputDir.mkdir()) {
                System.out.println(outputDir.getAbsoluteFile() + " created successfully.");
            } else {
                System.out.println("WARNING::directory creation failed");
            }
        }

        File inputDir = new File(inputFilesDirPath);
        File[] var5 = (File[])Objects.requireNonNull(inputDir.listFiles());
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            File fileEntry = var5[var7];
            if (!fileEntry.isDirectory() && !fileEntry.isHidden()) {
                System.out.println(fileEntry.getName());
                String var10000 = outputDir.getAbsolutePath();
                String outputFilePath = var10000 + "/" + FilenameUtils.removeExtension(fileEntry.getName()) + "_Validation";
                System.out.println("Validating data graph using " + fileEntry.getName());
                validate(dataFilePath, fileEntry.getAbsolutePath(), outputFilePath + ".ttl", outputFilePath);
            }
        }

    }

    private static void validate(String inputDataFilePath, String inputSHACLFilePath, String outputSHACLFilePath, String outputCSVFilePath) {
        try {
            Graph shapesGraph = RDFDataMgr.loadGraph(inputSHACLFilePath);
            Graph dataGraph = RDFDataMgr.loadGraph(inputDataFilePath);
            Shapes shapes = Shapes.parse(shapesGraph);
            System.out.println("Write validation report as output!");
            OutputStream out = new FileOutputStream(outputSHACLFilePath, false);
            RDFDataMgr.write(out, ShaclValidator.get().validate(shapes, dataGraph).getModel(), Lang.TTL);
            out.close();
            RepositoryConnection conn = readFileAsRdf4JModel(inputSHACLFilePath);
            FileWriter fileWriter = new FileWriter(outputCSVFilePath + ".csv", true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            FileWriter fileWriterNotConstraints = new FileWriter(outputCSVFilePath + "_NotConstraints.csv", true);
            PrintWriter printWriterNotConstraints = new PrintWriter(fileWriterNotConstraints);
            String header = "SourceShape|FocusNode|ResultPath|Value|SourceConstraintComponent|Message";
            String headerNotConstraints = "SourceShape|FocusNode|ResultPath|property|support|confidence|class";
            printWriter.println(header);
            printWriterNotConstraints.println(headerNotConstraints);
            System.out.println("Iterating over report entries");
            ShaclValidator.get().validate(shapes, dataGraph).getEntries().forEach((re) -> {
                Node var10000;
                String line;
                if (re.sourceConstraintComponent().getLocalName().equals("NotConstraintComponent")) {
                    var10000 = re.source();
                    line = "" + var10000 + "|" + re.focusNode() + "|" + re.resultPath();
                    String queryResult = executeQueryForOneClassType(conn, buildQuery(extractShNotShapeIri(re.message())));
                    if (Objects.equals(queryResult, "")) {
                        List<String> outputLines = executeQueryForMultipleClassType(conn, buildQueryForMultipleClassTypesPs(extractShNotShapeIri(re.message())));
                        Iterator var7 = outputLines.iterator();

                        while(var7.hasNext()) {
                            String result = (String)var7.next();
                            String x = line + "|" + result;
                            printWriterNotConstraints.println(x);
                        }
                    } else {
                        line = line + "|" + queryResult;
                        printWriterNotConstraints.println(line);
                    }
                } else {
                    var10000 = re.source();
                    line = "" + var10000 + "|" + re.focusNode() + "|" + re.resultPath() + "|" + re.value() + "|" + re.sourceConstraintComponent() + "|" + re.message();
                    printWriter.println(line);
                }

            });
            System.out.println("Closing files.");
            printWriterNotConstraints.close();
            printWriter.close();
            conn.close();
        } catch (Exception var15) {
            var15.printStackTrace();
        }

    }

    private static String extractShNotShapeIri(String message) {
        return message.replace("Not[NodeShape[", "").replace("]]", "").split(" ")[0];
    }

    private static RepositoryConnection readFileAsRdf4JModel(String inputFileAddress) {
        RepositoryConnection conn = null;

        try {
            InputStream input = new FileInputStream(inputFileAddress);
            Model model = Rio.parse(input, "", RDFFormat.TURTLE, new Resource[0]);
            Repository db = new SailRepository(new MemoryStore());
            conn = db.getConnection();
            conn.add(model, new Resource[0]);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return conn;
    }

    private static String executeQueryForOneClassType(RepositoryConnection conn, String queryString) {
        TupleQuery query = conn.prepareTupleQuery(queryString);
        String line = "";
        TupleQueryResult result = query.evaluate();

        try {
            Iterator var5 = result.iterator();

            while(var5.hasNext()) {
                BindingSet solution = (BindingSet)var5.next();
                Value var10000;
                if (solution.hasBinding("class")) {
                    var10000 = solution.getValue("property");
                    line = "" + var10000 + "|" + solution.getValue("support").stringValue() + "|" + solution.getValue("confidence").stringValue() + "|" + solution.getValue("class").stringValue();
                } else {
                    var10000 = solution.getValue("property");
                    line = "" + var10000 + "|" + solution.getValue("support").stringValue() + "|" + solution.getValue("confidence").stringValue() + "|NULL";
                }
            }

            result.close();
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

        return line;
    }

    private static List<String> executeQueryForMultipleClassType(RepositoryConnection conn, String queryString) {
        TupleQuery query = conn.prepareTupleQuery(queryString);
        List<String> outputLines = new ArrayList();
        TupleQueryResult result = query.evaluate();

        try {
            Iterator var5 = result.iterator();

            while(true) {
                if (!var5.hasNext()) {
                    result.close();
                    break;
                }

                BindingSet solution = (BindingSet)var5.next();
                Value var10000 = solution.getValue("property");
                String line = "" + var10000 + "|" + solution.getValue("support").stringValue() + "|" + solution.getValue("confidence").stringValue() + "|" + solution.getValue("class").stringValue();
                outputLines.add(line);
            }
        } catch (Throwable var9) {
            if (result != null) {
                try {
                    result.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }
            }

            throw var9;
        }

        if (result != null) {
            result.close();
        }

        return outputLines;
    }

    private static String buildQuery(String iri) {
        String queryTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nSELECT ?property ?support ?confidence ?class WHERE { \n \t<_NS_>  rdf:type  <http://www.w3.org/ns/shacl#NodeShape> .\n    <_NS_> ?p ?propertyShape .    \n    ?propertyShape rdf:type <http://www.w3.org/ns/shacl#PropertyShape> .\n    ?propertyShape <http://shaclshapes.org/support>  ?support. \n    ?propertyShape <http://shaclshapes.org/confidence> ?confidence .\n    Optional {?propertyShape <http://www.w3.org/ns/shacl#class> ?class .}\n    ?propertyShape   <http://www.w3.org/ns/shacl#path> ?property .\n}\n";
        return queryTemplate.replace("_NS_", iri);
    }

    private static String buildQueryForMultipleClassTypesPs(String iri) {
        String queryTemplate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\nSELECT ?property ?class ?support ?confidence   WHERE { \n \t<_NS_>  rdf:type  <http://www.w3.org/ns/shacl#NodeShape> .\n  <_NS_> ?p ?propertyShape .    \n  ?propertyShape rdf:type <http://www.w3.org/ns/shacl#PropertyShape> .\n\t?propertyShape <http://www.w3.org/ns/shacl#or>/rdf:rest*/rdf:first  ?orPsConstraints. \n\t\n\t?orPsConstraints <http://shaclshapes.org/confidence> ?confidence .\n  ?orPsConstraints <http://shaclshapes.org/support> ?support.\n  ?orPsConstraints  <http://www.w3.org/ns/shacl#class> ?class .\n  ?propertyShape   <http://www.w3.org/ns/shacl#path> ?property .\n}";
        return queryTemplate.replace("_NS_", iri);
    }

    public static void testValidation() {
        String inputDataFilePath = "/Users/kashifrabbani/Documents/GitHub/data/CityDBpedia.nt";
        String inputSHACLFilePath = "/Users/kashifrabbani/Documents/GitHub/qse/Output/TEMP/dbpedia_city.ttl";
        String outputSHACLFilePath = "/Users/kashifrabbani/Documents/GitHub/qse/validation/example/Output/valid.ttl";
        String outputCSVFilePath = "/Users/kashifrabbani/Documents/GitHub/qse/validation/example/Output/valid.csv";
        FilesUtil.deleteFile(outputSHACLFilePath);
        FilesUtil.deleteFile(outputCSVFilePath);
        validate(inputDataFilePath, inputSHACLFilePath, outputSHACLFilePath, outputCSVFilePath);
    }

    public static void main(String[] args) throws Exception {
        testValidation();
    }
}
