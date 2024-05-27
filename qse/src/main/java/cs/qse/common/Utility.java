//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import cs.Main;
import cs.qse.common.encoders.ConcurrentStringEncoder;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple3;
import cs.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.semanticweb.yars.nx.Literal;

public class Utility {
    public Utility() {
    }

    public static String extractObjectType(String literalIri) {
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

    public static String buildQuery(String entity, Set<String> types, String typePredicate) {
        StringBuilder query = new StringBuilder("PREFIX onto: <http://www.ontotext.com/> \nSELECT * from onto:explicit WHERE { \n");
        Iterator var4 = types.iterator();

        while(var4.hasNext()) {
            String type = (String)var4.next();
            query.append("<").append(entity).append("> ").append(typePredicate).append(" <").append(type).append("> .\n");
        }

        query.append("<").append(entity).append("> ").append("?p ?o . \n }\n");
        return query.toString();
    }

    public static String buildBatchQuery(Set<String> types, List<String> entities, String typePredicate) {
        StringBuilder query = new StringBuilder("PREFIX onto: <http://www.ontotext.com/> \nSELECT * from onto:explicit WHERE { \n");
        Iterator var4 = types.iterator();

        String entity;
        while(var4.hasNext()) {
            entity = (String)var4.next();
            query.append("?entity ").append(typePredicate).append(" <").append(entity).append("> .\n");
        }

        query.append("?entity ?p ?o . \n");
        query.append("VALUES (?entity) { \n");
        var4 = entities.iterator();

        while(var4.hasNext()) {
            entity = (String)var4.next();
            query.append("\t( ").append(entity).append(" ) \n");
        }

        query.append(" } \n} ");
        return query.toString();
    }

    private static String buildQuery(String classIri, String property, String objectType, String queryFile, String typePredicate) {
        String query = FilesUtil.readQuery(queryFile).replace(":Class", " <" + classIri + "> ").replace(":Prop", " <" + property + "> ").replace(":ObjectType", " " + objectType + " ");
        query = query.replace(":instantiationProperty", typePredicate);
        return query;
    }

    public static void writeSupportToFile(StringEncoder stringEncoder, Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport, Map<Integer, List<Integer>> sampledEntitiesPerClass) {
        System.out.println("Started writeSupportToFile()");
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            FileWriter fileWriter = new FileWriter(new File(Constants.TEMP_DATASET_FILE), false);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Iterator var6 = shapeTripletSupport.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var6.next();
                Tuple3<Integer, Integer, Integer> tupl3 = (Tuple3)entry.getKey();
                Integer count = ((SupportConfidence)entry.getValue()).getSupport();
                String log = stringEncoder.decode((Integer)tupl3._1) + "|" + stringEncoder.decode((Integer)tupl3._2) + "|" + stringEncoder.decode((Integer)tupl3._3) + "|" + count + "|" + ((List)sampledEntitiesPerClass.get(tupl3._1)).size();
                printWriter.println(log);
            }

            printWriter.close();
        } catch (IOException var11) {
            var11.printStackTrace();
        }

        watch.stop();
        Utils.logTime("writeSupportToFile() ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public static void writeSupportToFile(ConcurrentStringEncoder encoder, Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport, Map<Integer, List<Integer>> sampledEntitiesPerClass) {
        System.out.println("Started writeSupportToFile()");
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            FileWriter fileWriter = new FileWriter(new File(Constants.TEMP_DATASET_FILE), false);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Iterator var6 = shapeTripletSupport.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<Tuple3<Integer, Integer, Integer>, SupportConfidence> entry = (Map.Entry)var6.next();
                Tuple3<Integer, Integer, Integer> tupl3 = (Tuple3)entry.getKey();
                Integer count = ((SupportConfidence)entry.getValue()).getSupport();
                String log = encoder.decode((Integer)tupl3._1) + "|" + encoder.decode((Integer)tupl3._2) + "|" + encoder.decode((Integer)tupl3._3) + "|" + count + "|" + ((List)sampledEntitiesPerClass.get(tupl3._1)).size();
                printWriter.println(log);
            }

            printWriter.close();
        } catch (IOException var11) {
            var11.printStackTrace();
        }

        watch.stop();
        Utils.logTime("writeSupportToFile() ", TimeUnit.MILLISECONDS.toSeconds(watch.getTime()), TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public static Map<Integer, Map<Integer, Set<Integer>>> extractShapesForSpecificClasses(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Map<Integer, Integer> classEntityCount, StringEncoder stringEncoder) {
        Map<Integer, Map<Integer, Set<Integer>>> filteredClassToPropWithObjTypes = new HashMap();
        String fileAddress = Main.configDirPath + "pruning/classes.txt";
        List<String> classes = FilesUtil.readAllLinesFromFile(fileAddress);
        return processClasses(classToPropWithObjTypes, classEntityCount, stringEncoder, filteredClassToPropWithObjTypes, classes);
    }

    private static Map<Integer, Map<Integer, Set<Integer>>> processClasses(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Map<Integer, Integer> classEntityCount, StringEncoder stringEncoder, Map<Integer, Map<Integer, Set<Integer>>> filteredClassToPropWithObjTypes, List<String> classes) {
        classes.forEach((classIri) -> {
            int key = stringEncoder.encode(classIri);
            Map<Integer, Set<Integer>> value = (Map)classToPropWithObjTypes.get(key);
            if (classEntityCount.containsKey(key)) {
                filteredClassToPropWithObjTypes.put(key, value);
            }

        });
        return filteredClassToPropWithObjTypes;
    }

    public static Map<Integer, Map<Integer, Set<Integer>>> extractShapesForSpecificClasses(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Map<Integer, Integer> classEntityCount, StringEncoder stringEncoder, List<String> classes) {
        Map<Integer, Map<Integer, Set<Integer>>> filteredClassToPropWithObjTypes = new HashMap();
        return processClasses(classToPropWithObjTypes, classEntityCount, stringEncoder, filteredClassToPropWithObjTypes, classes);
    }

    public static List<String> getListOfClasses() {
        String fileAddress = Main.configDirPath + "pruning/classes.txt";
        return FilesUtil.readAllLinesFromFile(fileAddress);
    }

    public static void writeClassFrequencyInFile(Map<Integer, Integer> classEntityCount, StringEncoder stringEncoder) {
        String fileNameAndPath = Main.outputFilePath + "/classFrequency.csv";

        try {
            FileWriter fileWriter = new FileWriter(fileNameAndPath, false);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Class,Frequency");
            classEntityCount.forEach((classVal, entityCount) -> {
                String var10001 = stringEncoder.decode(classVal);
                printWriter.println(var10001 + "," + entityCount);
            });
            printWriter.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public static RepositoryConnection readFileAsRdf4JModel(String inputFileAddress) {
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
}
