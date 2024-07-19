import cs.Main;
import cs.qse.common.EntityData;
import cs.qse.common.TurtlePrettyFormatter;
import cs.qse.filebased.Parser;
import cs.qse.filebased.SupportConfidence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.DiffExtractor;
import qseevolvingkg.partialsparqlqueries.DiffShapeGenerator;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;
import qseevolvingkg.partialsparqlqueries.utils.RegexUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class RQ2UnitTests {
    String instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

    @Before
    public void prepareQSE() throws IOException {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(0,0)}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.saveCountInPropertyData=true;
        Path basePath = Paths.get( "Output", "UnitTestOutput");
        cs.Main.setOutputFilePathForJar(basePath.toAbsolutePath()+File.separator);

        Files.walk(Paths.get(Main.outputFilePath))
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
        var datasetPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\notes\\defaultGraphs\\miniexample\\People2AdaptWithMultipleKnows.nt";

        Parser parser = new Parser(datasetPath, 3, 10, instanceTypeProperty);
        runParser(parser);
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
        var diffMap = diffShapeGenerator.generateDiffMap();
        assertTrue(diffMap.size()==1);
        assertTrue(diffMap.containsKey(6));
        assertTrue(diffMap.get(6).size()==2);
        assertTrue(diffMap.get(6).containsKey(1));
        assertTrue(diffMap.get(6).get(1).size()==1);
        assertTrue(diffMap.get(6).get(1).contains(2));
        assertTrue(diffMap.get(6).containsKey(7));
        assertTrue(diffMap.get(6).get(7).size()==1);
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
        var diffMap = diffShapeGenerator.generateDiffMap();
        assertTrue(diffMap.size()==1);
        assertTrue(diffMap.containsKey(1));
        assertTrue(diffMap.get(1).size()==1);
        assertTrue(diffMap.get(1).containsKey(6));
        assertTrue(diffMap.get(1).get(6).size()==1);
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
        var diffMap = diffShapeGenerator.generateDiffMap();
        ExtractedShapes extractedShapes = diffShapeGenerator.generateDiffShapesWithQse(diffMap);
        String file = extractedShapes.getFileAsString();
        var catshape = RegexUtils.getShapeAsString("http://shaclshapes.org/CatShape", file);
        assertTrue(!catshape.isEmpty());
        var ageShape = RegexUtils.getShapeAsString("http://shaclshapes.org/ageCatShapeProperty", file);
        assertTrue(!ageShape.isEmpty());
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
        var diffMap = diffShapeGenerator.generateDiffMap();
        diffShapeGenerator.generateDiffShapesWithQse(diffMap);
        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString(diffExtractor.originalExtractedShapes);
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("colorCatShape"));
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
        var diffMap = diffShapeGenerator.generateDiffMap();
        diffShapeGenerator.generateDiffShapesWithQse(diffMap);
        var newFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString(diffExtractor.originalExtractedShapes);
        var fileAsString = RegexUtils.getFileAsString(newFilePath);
        assertTrue(fileAsString.contains("colorCatShape"));
        assertTrue(fileAsString.contains("<http://www.w3.org/ns/shacl#property> <http://shaclshapes.org/colorCatShapeProperty> ;"));
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
