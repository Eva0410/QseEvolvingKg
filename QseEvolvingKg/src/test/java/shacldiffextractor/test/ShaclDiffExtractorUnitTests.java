package shacldiffextractor.test;

import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.filebased.Parser;
import cs.qse.filebased.SupportConfidence;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import shape_comparator.data.ExtractedShapes;
import shacldiffextractor.diff.DiffExtractor;
import shacldiffextractor.diff.DiffManager;
import shacldiffextractor.diff.DiffShapeGenerator;
import sparqlshapechecker.utils.ConfigManager;
import sparqlshapechecker.utils.RegexUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

//failed tests need to be rerun, there are some issues with file locks todo
public class ShaclDiffExtractorUnitTests {
    String instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

    @Before
    public void prepareQSE() throws IOException {
        Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(0,0)}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.saveCountInPropertyData=true;
        Path basePath = Paths.get( "Output", "UnitTestOutput", "Original");
        Files.createDirectories(basePath);
        Main.setOutputFilePathForJar(basePath.toAbsolutePath()+File.separator);

        Files.walk(Paths.get(Main.outputFilePath).getParent())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                }
        });
    }


    @Test
    public void runQseWithPeople() {
        Main.datasetName = "People2";
        var datasetPath = System.getProperty("user.dir")+"\\notes\\defaultGraphs\\miniexample\\People2AdaptWithMultipleKnows.nt";

        Parser parser = new Parser(datasetPath, 3, 10, instanceTypeProperty);
        runParser(parser);
    }

    @Test
    public void runQseWithPeopleWithSupport() throws IOException {
        Main.datasetName = "People3";
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
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        var tempFileNew = Files.createTempFile("QSERQ2TmpFileNew", ".nt");
        Files.write(tempFileNew, contentNew.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        Main.setPruningThresholds("{(0,0)}");
        Parser parser = new Parser(tempFileNew.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        parser.entityExtraction();
        parser.entityConstraintsExtraction();
        parser.computeSupportConfidence();
        parser.extractSHACLShapes(true, Main.qseFromSpecificClasses);
    }

    @Test
    public void runQseWithPeople2() throws IOException {
        Main.datasetName = "People2";
        var tempFileNew = Files.createTempFile("QSERQ2TmpFileNew", ".nt");
        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        Files.write(tempFileNew, contentNew.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        Parser parser = new Parser(tempFileNew.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        runParser(parser);
    }

    public DiffExtractor testQseOuptut(String content, String contentNew, String contentAdded, String contentDeleted) throws IOException {
        Main.datasetName = "People2";
        int support = 0;
        double confidence = 0.0;

        var tempFile = Files.createTempFile("QSERQ2TmpFile", ".nt");
        Files.write(tempFile, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileNew = Files.createTempFile("QSERQ2TmpFileNew", ".nt");
        Files.write(tempFileNew, contentNew.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileAdded = Files.createTempFile("QSERQ2TmpFileAdded", ".nt");
        Files.write(tempFileAdded, contentAdded.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        var tempFileDeleted = Files.createTempFile("QSERQ2TmpFileDeleted", ".nt");
        Files.write(tempFileDeleted, contentDeleted.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        Parser parser = new Parser(tempFile.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        runParser(parser);
        DiffExtractor diffExtractor = new DiffExtractor(tempFileAdded.toAbsolutePath().toString(),tempFileDeleted.toAbsolutePath().toString(),parser,support, confidence);
        diffExtractor.extractFromFile();

        var oldDataSetName = Main.datasetName;
        var oldOutputPath = Main.outputFilePath;
        Main.datasetName = Main.datasetName+"_Full";
        Main.outputFilePath = Main.outputFilePath+ "Full" + File.separator;
        Parser parserV3 = new Parser(tempFileNew.toAbsolutePath().toString(), 3, 10, instanceTypeProperty);
        parserV3.setStringEncoder(parser.getStringEncoder());
        runParser(parserV3);
        Main.datasetName = oldDataSetName;
        Main.outputFilePath = oldOutputPath;

        assertTrue(parser.classEntityCount.equals(parserV3.classEntityCount));
        assertTrue(areMapsEqual(parser.entityDataHashMap, parserV3.entityDataHashMap));
        assertTrue(areMapsEqual(parser.classToPropWithObjTypes, parserV3.classToPropWithObjTypes));
        assertTrue(areMapsEqual(parser.statsComputer.getShapeTripletSupport(), parserV3.statsComputer.getShapeTripletSupport()));
        return diffExtractor;
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
    public void testDeleteSameOutcome() throws IOException {

        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n";

        var contentAdded = "";

        var contentDeleted = "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n";

        testQseOuptut(content, contentNew, contentAdded, contentDeleted);
    }

    @Test
    public void testDeleteOnePropertyLeft() throws IOException {

        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n" +
                "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/name> \"Alice\" .\n";

        var contentAdded = "";

        var contentDeleted = "<http://example.org/bob> <http://xmlns.com/foaf/0.1/name> \"Bob\" .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/alice> .\n" +
                "<http://example.org/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.org/bob> .\n";

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
    public void addCheckDiffMapAddNodeShape() throws IOException {
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

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        assertEquals(1, diffMap.size());
        assertTrue(diffMap.containsKey(6));
        assertEquals(2, diffMap.get(6).size());
        assertTrue(diffMap.get(6).containsKey(1));
        assertEquals(1, diffMap.get(6).get(1).size());
        assertTrue(diffMap.get(6).get(1).contains(2));
        assertTrue(diffMap.get(6).containsKey(7));
        assertEquals(1, diffMap.get(6).get(7).size());
        assertTrue(diffMap.get(6).get(7).contains(4));
    }

    @Test
    public void addCheckDiffMapAddPropertyShape() throws IOException {
        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n" +
                "<http://example.org/orangeCat> <http://example.org/age> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";

        var contentAdded = "<http://example.org/orangeCat> <http://example.org/age> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";
        var contentDeleted = "";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        assertEquals(1, diffMap.size());
        assertTrue(diffMap.containsKey(1));
        assertEquals(1, diffMap.get(1).size());
        assertTrue(diffMap.get(1).containsKey(6));
        assertEquals(1, diffMap.get(1).get(6).size());
        assertTrue(diffMap.get(1).get(6).contains(7));
    }

    @Test
    public void addCheckDiffShapeFile() throws IOException {
        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n" +
                "<http://example.org/orangeCat> <http://example.org/age> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";

        var contentAdded = "<http://example.org/orangeCat> <http://example.org/age> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";
        var contentDeleted = "";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        ExtractedShapes extractedShapes = diffShapeGenerator.generateDiffShapesWithQse();
        String file = extractedShapes.getFileAsString(true);
        var catShapeString = RegexUtils.getShapeAsString("http://shaclshapes.org/CatShape", file);
        assertFalse(catShapeString.isEmpty());
        var ageShapeString = RegexUtils.getShapeAsString("http://shaclshapes.org/ageCatShapeProperty", file);
        assertFalse(ageShapeString.isEmpty());
    }

    @Test
    public void addDiffNSToExistingShaclFile() throws IOException {
        var content = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n" +
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        var contentAdded = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        var contentDeleted = "";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        diffShapeGenerator.generateDiffShapesWithQse();
        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("colorCatShape"));

        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),2);
        var catShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/CatShape")).toList().get(0);
        var personShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/PersonShape")).toList().get(0);
        assertNotNull(catShape);
        assertNotNull(personShape);
        assertEquals(personShape.propertyShapes.size(),1);
        var colorShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("colorCatShapeProperty")).toList().get(0);
        assertNotNull(colorShape);
        var instanceTypeShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("instanceTypeCatShapeProperty")).toList().get(0);
        assertNotNull(instanceTypeShape);
        assertEquals(catShape.propertyShapes.size(),2);
    }

    @Test
    public void addDiffPSToExistingShaclFile() throws IOException {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";

        var contentNew = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var contentAdded = "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        var contentDeleted = "";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        diffShapeGenerator.generateDiffShapesWithQse();
        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("colorCatShape"));
        assertTrue(fileAsString.contains("<http://www.w3.org/ns/shacl#property> <http://shaclshapes.org/colorCatShapeProperty> ;"));

        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),1);
        var catShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/CatShape")).toList().get(0);
        assertNotNull(catShape);
        var colorShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("colorCatShapeProperty")).toList().get(0);
        assertNotNull(colorShape);
        assertEquals(catShape.propertyShapes.size(),2);
    }

    @Test
    public void constraintAddedInPropertyShape() throws IOException {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var contentNew = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";
        var contentAdded = "<http://example.org/orangeCat> <http://example.org/color> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";
        var contentDeleted = "";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        diffShapeGenerator.generateDiffShapesWithQse();

        assertTrue(diffExtractor.editedShapesMap.containsKey(0));
        assertEquals(1, diffExtractor.editedShapesMap.size());
        assertEquals(1, diffExtractor.editedShapesMap.get(0).size());
        assertTrue(diffExtractor.editedShapesMap.get(0).contains(3));
        assertTrue(diffMap.containsKey(0));
        assertEquals(1, diffMap.size());
        assertEquals(1, diffMap.get(0).size());
        assertNotNull(diffMap.get(0).get(3));

        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("<http://shaclshapes.org/colorCatShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#minCount> 1 ;\n" +
                "  <http://www.w3.org/ns/shacl#or> ( [\n" +
                "    <http://shaclshapes.org/confidence> 1E0 ;\n" +
                "    <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "    <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "    <http://www.w3.org/ns/shacl#datatype> xsd:string ;\n" +
                "  ] [\n" +
                "    <http://shaclshapes.org/confidence> 1E0 ;\n" +
                "    <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "    <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "    <http://www.w3.org/ns/shacl#datatype> xsd:integer ;\n" +
                "  ] ) ;\n" +
                "  <http://www.w3.org/ns/shacl#path> <http://example.org/color> .\n"));


        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),1);
        var catShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/CatShape")).toList().get(0);
        assertNotNull(catShape);
        var colorShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("colorCatShapeProperty")).toList().get(0);
        assertNotNull(colorShape);
        assertEquals(catShape.propertyShapes.size(),2);
    }

    @Test
    public void constraintDeletedInPropertyShape() throws IOException {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";

        var contentNew = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n" +
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";
        var contentAdded = "";
        var contentDeleted = "<http://example.org/orangeCat> <http://example.org/color> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        diffShapeGenerator.generateDiffShapesWithQse();

        assertTrue(diffExtractor.editedShapesMap.containsKey(0));
        assertEquals(1, diffExtractor.editedShapesMap.size());
        assertEquals(1, diffExtractor.editedShapesMap.get(0).size());
        assertTrue(diffExtractor.editedShapesMap.get(0).contains(3));
        assertTrue(diffMap.containsKey(0));
        assertEquals(1, diffMap.size());
        assertEquals(1, diffMap.get(0).size());
        assertNotNull(diffMap.get(0).get(3));

        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("<http://shaclshapes.org/colorCatShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://shaclshapes.org/confidence> 1E0 ;\n" +
                "  <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "  <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "  <http://www.w3.org/ns/shacl#datatype> xsd:string ;\n" +
                "  <http://www.w3.org/ns/shacl#minCount> 1 ;\n" +
                "  <http://www.w3.org/ns/shacl#path> <http://example.org/color> ."));

        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),1);
        var catShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/CatShape")).toList().get(0);
        assertNotNull(catShape);
        var colorShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("colorCatShapeProperty")).toList().get(0);
        assertNotNull(colorShape);
        assertEquals(catShape.propertyShapes.size(),2);
    }

    @Test
    public void deleteNSFromShaclFile() throws IOException {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n"+
                "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";
        var contentAdded = "";
        var contentDeleted = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.computeDeletedShapes();
        var deletedShapeMap = diffShapeGenerator.deletedShapes;
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        diffShapeGenerator.generateDiffShapesWithQse();

        assertTrue(deletedShapeMap.containsKey(0));
        assertEquals(1, deletedShapeMap.size());
        assertEquals(2, deletedShapeMap.get(0).size());
        assertTrue(deletedShapeMap.get(0).contains(2));
        assertTrue(deletedShapeMap.get(0).contains(4));
        assertEquals(0, diffMap.size());

        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        var fileWithDeletedShapes = diffShapeGenerator.deleteShapesFromFile(fileAsString);
        RegexUtils.saveStringAsFile(fileWithDeletedShapes,  Main.outputFilePath+"finished.ttl");
        assertFalse(fileWithDeletedShapes.contains("<http://shaclshapes.org/CatShape>"));
        assertFalse(fileWithDeletedShapes.contains("<http://shaclshapes.org/colorCatShapeProperty>"));
        assertFalse(fileWithDeletedShapes.contains("<http://shaclshapes.org/instanceTypeCatShapeProperty>"));

        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),1);
        var personShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/PersonShape")).toList().get(0);
        assertNotNull(personShape);
        assertEquals(personShape.propertyShapes.size(),1);
        var instanceTypeShape = personShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("instanceTypePersonShapeProperty")).toList().get(0);
        assertNotNull(instanceTypeShape);
        assertEquals(personShape.propertyShapes.size(),1);
    }

    @Test
    public void deletePSFromShaclFile() throws IOException {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n"+
                "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n"+
                "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";

        var contentNew = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n"+
                "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";
        var contentAdded = "";
        var contentDeleted = "<http://example.org/orangeCat> <http://example.org/color> \"orange\" .\n";

        var diffExtractor = testQseOuptut(content, contentNew, contentAdded, contentDeleted);
        DiffShapeGenerator diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.computeDeletedShapes();
        var deletedShapeMap = diffShapeGenerator.deletedShapes;
        diffShapeGenerator.generateDiffMap();
        var diffMap = diffShapeGenerator.diffShapes;
        diffShapeGenerator.generateDiffShapesWithQse();

        assertTrue(deletedShapeMap.containsKey(0));
        assertEquals(1, deletedShapeMap.size());
        assertEquals(1, deletedShapeMap.get(0).size());
        assertTrue(deletedShapeMap.get(0).contains(4));
        assertEquals(0, diffMap.size());

        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        var fileWithDeletedShapes = diffShapeGenerator.deleteShapesFromFile(fileAsString);
        RegexUtils.saveStringAsFile(fileWithDeletedShapes,  Main.outputFilePath+"finished.ttl");
        assertTrue(fileWithDeletedShapes.contains("<http://shaclshapes.org/CatShape>"));
        assertFalse(fileWithDeletedShapes.contains("<http://shaclshapes.org/colorCatShapeProperty>"));
        assertTrue(fileWithDeletedShapes.contains("<http://shaclshapes.org/instanceTypeCatShapeProperty>"));

        var nodeShapes = diffShapeGenerator.resultExtractedShapes.nodeShapes;
        assertEquals(nodeShapes.size(),2);
        var catShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/CatShape")).toList().get(0);
        var personShape = nodeShapes.stream().filter(ns -> ns.getIri().toString().contains("http://shaclshapes.org/PersonShape")).toList().get(0);
        assertNotNull(catShape);
        assertNotNull(personShape);
        assertEquals(personShape.propertyShapes.size(),1);
        var instanceCatShape = catShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("instanceTypeCatShapeProperty")).toList().get(0);
        var instancePerson = personShape.propertyShapes.stream().filter(ps -> ps.iri.toString().contains("instanceTypePersonShapeProperty")).toList().get(0);
        assertNotNull(instanceCatShape);
        assertNotNull(instancePerson);
        assertEquals(catShape.propertyShapes.size(),1);
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

    @Test
    public void testDiffManager()
    {
        var content = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";

        var contentAdded = "<http://example.org/alice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .\n";
        var contentDeleted = "<http://example.org/orangeCat> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Cat> .\n";
        String filePath = generateFile(content);
        String addedPath = generateFile(contentAdded);
        String deletedPath = generateFile(contentDeleted);

        DiffManager diffManager = new DiffManager();
        var extractedShapes = diffManager.run("People", filePath, addedPath, deletedPath);
        assertTrue(extractedShapes.getFileAsString(true).contains("<http://shaclshapes.org/PersonShape>"));
        assertTrue(extractedShapes.getFileAsString(true).contains("<http://shaclshapes.org/instanceTypePersonShapeProperty>"));
        var nodeShapes = extractedShapes.getNodeShapes();
        assertEquals(1, nodeShapes.size());
        assertTrue(nodeShapes.get(0).getIri().toString().contains("http://shaclshapes.org/PersonShape"));
        assertEquals(nodeShapes.get(0).propertyShapes.size(),1);
        assertTrue(nodeShapes.get(0).propertyShapes.get(0).iri.toString().contains("http://shaclshapes.org/instanceTypePersonShapeProperty"));
    }

    static String generateFile(String content) {
        Path tempFile = null;
        try {
            var randomString =  RandomStringUtils.random(5, true, true);
            tempFile = Files.createTempFile("QSEDiff"+randomString,".nt");
            Files.write(tempFile, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            return tempFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
