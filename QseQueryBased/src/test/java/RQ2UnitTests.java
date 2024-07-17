import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.filebased.Parser;
import cs.qse.filebased.SupportConfidence;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.DiffExtractor;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class RQ2UnitTests {
    String instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

    public void prepareQSE() {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(0,0)}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.saveCountInPropertyData=true;
        cs.Main.setOutputFilePathForJar("/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/");
    }
    @Test
    public void runQseWithPeople() {
        prepareQSE();
        Main.datasetName = "People2";
        var datasetPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\People2AdaptWithMultipleKnows.nt";

        Parser parser = new Parser(datasetPath, 3, 10, instanceTypeProperty);
        runParser(parser);
    }

    public void testQseOuptut(String content, String contentNew, String contentAdded, String contentDeleted) throws IOException {
        prepareQSE();
        Main.datasetName = "People2";
        int support = 0;
        double confidence = 0.0;

        var tempFile = Files.createTempFile("QSERQ2TmpFile", ".nt");
        Files.write(tempFile, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileNew = Files.createTempFile("QSERQ2TmpFile", ".nt");
        Files.write(tempFileNew, contentNew.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileAdded = Files.createTempFile("QSERQ2TmpFileAdded", ".nt");
        Files.write(tempFileAdded, contentAdded.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileDeleted = Files.createTempFile("QSERQ2TmpFileDeleted", ".nt");
        Files.write(tempFileDeleted, contentDeleted.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        Parser parser = new Parser(tempFile.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        runParser(parser);
        DiffExtractor diffExtractor = new DiffExtractor(tempFileAdded.toAbsolutePath().toString(),tempFileDeleted.toAbsolutePath().toString(),parser,support, confidence);
        diffExtractor.extractFromFile();

        Parser parserV3 = new Parser(tempFileNew.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        parserV3.setStringEncoder(parser.getStringEncoder());
        runParser(parserV3);

        assertTrue(parser.classEntityCount.equals(parserV3.classEntityCount));
        assertTrue(areMapsEqual(parser.entityDataHashMap, parserV3.entityDataHashMap));
        assertTrue(areMapsEqual(parser.classToPropWithObjTypes, parserV3.classToPropWithObjTypes));
        assertTrue(areMapsEqual(parser.statsComputer.getShapeTripletSupport(), parserV3.statsComputer.getShapeTripletSupport()));
    }

    @Test
    public void testBasicDelete() throws IOException {

        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/orangeCat> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n";

        var contentAdded = "";

        var contentDeleted = "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/orangeCat> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";

        testQseOuptut(content, contentNew, contentAdded, contentDeleted);
    }

    @Test
    public void testBasicAdd() throws IOException {

        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/jenny> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/name> \"Jenny\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/jenny> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/name> \"Jenny\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/blackCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/greyCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n" +
                "<http://example.org/blackCat> <http://example.org/color> \"black\" .\n" +
                "<http://example.org/greyCat> <http://example.org/color> \"grey\" .\n";

        var contentAdded = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/blackCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/greyCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n" +
                "<http://example.org/blackCat> <http://example.org/color> \"black\" .\n" +
                "<http://example.org/greyCat> <http://example.org/color> \"grey\" .";

        var contentDeleted = "";

        testQseOuptut(content, contentNew, contentAdded, contentDeleted);
    }

    @Test
    public void testBasicDeleteAndAdd() throws IOException {

        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/jenny> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/name> \"Jenny\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/blackCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/greyCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n" +
                "<http://example.org/blackCat> <http://example.org/color> \"black\" .\n" +
                "<http://example.org/greyCat> <http://example.org/color> \"grey\" .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/jenny> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/name> \"Jenny\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/orangeCat> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/blackCat> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/greyCat> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/blackCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/greyCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .";

        var contentAdded = "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/orangeCat> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/blackCat> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/greyCat> .";

        var contentDeleted = "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/jenny> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/blackCat> <http://example.org/color> \"black\" .\n" +
                "<http://example.org/greyCat> <http://example.org/color> \"grey\" .";

        testQseOuptut(content, contentNew, contentAdded, contentDeleted);
    }

    private void runParser(Parser parser) {
        parser.entityExtraction();
        parser.entityConstraintsExtraction();
        parser.computeSupportConfidence();
        parser.extractSHACLShapes(false, Main.qseFromSpecificClasses);
    }

    public static <K, V> boolean areMapsEqual(Map<K, V> map1, Map<K, V> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }

        for (Map.Entry<K, V> entry : map1.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (!map2.containsKey(key)) {
                return false;
            }
            if(value instanceof EntityData) {
                var value1 = (EntityData)value;
                var value2 = (EntityData) map2.get(key);
                if(!value1.classTypes.equals(value2.classTypes)) {
                    System.out.println("EntityData classTypes for " + key + " not equal");
                    return false;
                }
                if(!areMapsEqual(value1.propertyConstraintsMap, value2.propertyConstraintsMap)) {
                    return false;
                }
            }
            else if(value instanceof EntityData.PropertyData) {
                var value1 = (EntityData.PropertyData)value;
                var value2 = (EntityData.PropertyData) map2.get(key);
                if(!value1.objTypes.equals(value2.objTypes)) {
                    System.out.println("PropertyData objTypes " + value1.objTypes + " not equal");
                    return false;
                }
                if(value1.count!=value2.count) {
                    System.out.println("PropertyData count" + value1.count + " not equal");
                    return false;
                }
                if(!areMapsEqual(value1.objTypesCount, value2.objTypesCount)) {
                    return false;
                }
            }
            else if(value instanceof SupportConfidence) {
                var value1 = (SupportConfidence)value;
                var value2 = (SupportConfidence)map2.get(key);
                if(!Objects.equals(value1.getSupport(), value2.getSupport()))
                    return false;
                if(!Objects.equals(value1.getConfidence(), value2.getConfidence()))
                    return false;
            }
            else if (!value.equals(map2.get(key))) {
                return false;
            }
        }

        return true;
    }

}
