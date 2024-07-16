import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.filebased.Parser;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.DiffExtractor;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class RQ2UnitTests {
    @Test
    public void runQseWithPeople() {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(0,0)}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.datasetName = "People2";
        cs.Main.setOutputFilePathForJar("/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/");
        var datasetPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\People2.nt";
        var instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

        Parser parser = new Parser(datasetPath, 3, 10, instanceTypeProperty);
        parser.run();
    }

    @Test
    public void test() {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(0,0)}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.datasetName = "People2";
        cs.Main.setOutputFilePathForJar("/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/");
        var datasetPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\People2.nt";
        var datasetPathNew = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\People2.nt";
        var instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
        int support = 0;

        var addedFilePath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\Diff\\People2People3Added.nt";
        var deletedFilePath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\Diff\\People2People3Deleted.nt";
        Parser parser = new Parser(datasetPath, 3, 10, instanceTypeProperty);
        runParser(parser);
        DiffExtractor diffExtractor = new DiffExtractor(addedFilePath,deletedFilePath,parser,support);
        diffExtractor.extractFromFile();

        Parser parserV3 = new Parser(datasetPathNew, 3, 10, instanceTypeProperty);
        runParser(parserV3);

        assertTrue(parser.classEntityCount.equals(parserV3.classEntityCount));
//        assertTrue(areMapsEqual(parser.entityDataHashMap, parserV3.entityDataHashMap));
//        assertTrue(areMapsEqual(parser.classToPropWithObjTypes, parserV3.classToPropWithObjTypes));
        assertTrue(areMapsEqual(parser.statsComputer.getShapeTripletSupport(), parserV3.statsComputer.getShapeTripletSupport()));
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
            }
            else if (!value.equals(map2.get(key))) {
                return false;
            }
        }

        return true;
    }

}
