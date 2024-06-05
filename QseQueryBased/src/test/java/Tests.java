import cs.Main;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.GraphDbUtils;
import qseevolvingkg.partialsparqlqueries.RegexUtils;
import qseevolvingkg.partialsparqlqueries.ShaclOrListItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tests {
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/qse/src/main/resources";
    public static final String firstVersionName = "film";

    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
    public static final String pruningThresholds = "{(-1,0)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";


    @Test
    public void runQSEQBased() {
        Main.setResourcesPathForJar(resourcesPath);
        Main.setOutputFilePathForJar(outputPath);
        Main.setPruningThresholds(pruningThresholds);
        Main.annotateSupportConfidence = "true";
        Main.datasetName = firstVersionName;

        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();
    }

    @Test
    public void test() {
        Main.setResourcesPathForJar(resourcesPath);
        Main.setOutputFilePathForJar(outputPath);
        Main.setPruningThresholds(pruningThresholds);
        Main.annotateSupportConfidence = "true";
        Main.datasetName = firstVersionName;

//        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
//        qbParser.run();
//        var localPath = qbParser.dbDefaultConnectionString;
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        result.forEach(r -> System.out.println(r));
        System.out.println("new version");
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film3", result);
        result.forEach(r -> System.out.println(r));

    }

    @Test
    public void testNewRepo() {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var goalRepo = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\secondVersion";
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(goalRepo);
        result.forEach(r -> System.out.println(r));
    }

    @Test
    public void testDeleteInNewRepo() {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        extractedShapes.nodeShapes = result;
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film3", result);
        result.forEach(r -> System.out.println(r));
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_copy.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;
        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

//        var targetDb = graphDbUtils.cloneSailRepository(localPath, "secondVersion");
//        graphDbUtils.deleteFromRepoWhereSupportIsZero(targetDb, result);
//        var goalRepo = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\secondVersion";
//        var result2 = graphDbUtils.getNodeShapesWithTargetClassFromFile(goalRepo);
//        System.out.println("new version");
//        result2.forEach(r -> System.out.println(r));
    }

    @Test
    public void generateShaclTtlFromDb() {
        Main.setResourcesPathForJar(resourcesPath);
        Main.setOutputFilePathForJar(outputPath);
        Main.setPruningThresholds(pruningThresholds);
        Main.annotateSupportConfidence = "true";
        Main.datasetName = firstVersionName + "_cloned";
        var goalRepo = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\secondVersion";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.constructDefaultShapes(goalRepo);
    }

    @Test
    public void testDeleteWithPropertyShapeLiteralInNewRepo() {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.nodeShapes = result;
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "Film-NoGender", result);
        result.forEach(r -> System.out.println(r));
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_copyPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;
        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);
    }

    @Test
    public void deleteWithPropertyShapeIriInNewRepo() throws IOException {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film-NoSubPropertyOfSymmetricProperty", result);
        RegexUtils regexUtils = new RegexUtils();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.nodeShapes = result;
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;
        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

        assertTrue("Files are not equal", compareFiles(copiedFile,
                "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl"));
    }

    //todo implement, with shacl or items with literal and iri. make test reproducable
    @Test
    public void deleteWithPropertyShapeNestedItemIriInNewRepo() throws IOException {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film-NoSubPropertyOfSymmetricProperty", result);
        result.forEach(r -> System.out.println(r));
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.nodeShapes = result;
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;
        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);
    }

    @Test
    public void deleteFromOrItems() {
        //actually SHACL should not contain or list anymore, when only one item is left
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        RegexUtils regexUtils = new RegexUtils();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.fileContentPath = sourceFile;
        String shape = regexUtils.getShapeAsString("http://shaclshapes.org/labelGenreShapeProperty", extractedShapes.getFileAsString());
        ShaclOrListItem orListItem = new ShaclOrListItem(SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#Literal"),null,SimpleValueFactory.getInstance().createIRI("xsd:string"));
        String deletedShape = regexUtils.deleteShaclOrItemWithIriFromString(orListItem, shape, false);
        var expected = "\n<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#or> ([\n" +
                "    <http://shaclshapes.org/confidence> 1,6667E-1 ;\n" +
                "    <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "    <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "    <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "  ] ) ;\n" +
                "  <http://www.w3.org/ns/shacl#path> rdfs:label .\n";
        System.out.println(expected);
        System.out.println(deletedShape);
        assertEquals("Finished shapes do not match",expected, deletedShape);
    }

    @Test
    public void testDeleteWhenOnlyOneOrItemIsLeftWithGivenShape() {
        var shape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "\n<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#or> ([\n" +
                "    <http://shaclshapes.org/confidence> 1.6667E-1 ;\n" + //problem with , in double
                "    <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "    <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "    <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "  ] ) ;\n" +
                "  <http://www.w3.org/ns/shacl#path> rdfs:label .\n";
        var deletedShape = GraphDbUtils.deleteOrListAndConnectToParentNode(shape, "http://shaclshapes.org/labelGenreShapeProperty", 1);

        var expected = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "" +
                "\n<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://shaclshapes.org/confidence> 1,6667E-1 ;\n" + //problem with , in double
                "  <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "  <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "  <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "  <http://www.w3.org/ns/shacl#path> rdfs:label .";
        System.out.println(expected);
        System.out.println(deletedShape);
        assertEquals("Finished shapes do not match",expected.replace("\r\n", "\n"), deletedShape.replace("\r\n", "\n"));
    }

    @Test
    public void testDeleteWhenOnlyOneOrItemIsLeft() {
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        RegexUtils regexUtils = new RegexUtils();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.fileContentPath = sourceFile;
        extractedShapes.getFileAsString(); //read prefix lines todo maybe optimization
        String shape = regexUtils.getShapeAsString("http://shaclshapes.org/labelGenreShapeProperty", regexUtils.getFileAsString(sourceFile));
        ShaclOrListItem orListItem = new ShaclOrListItem(SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#Literal"),null,SimpleValueFactory.getInstance().createIRI("xsd:string"));
        String deletedShape = regexUtils.deleteShaclOrItemWithIriFromString(orListItem, shape, false);
        shape = extractedShapes.prefixLines + deletedShape;
        var adaptedShape = GraphDbUtils.deleteOrListAndConnectToParentNode(shape, "http://shaclshapes.org/labelGenreShapeProperty", 1);
        //todo remove prefix lines of shape
        var shapeWithoutPrefix = RegexUtils.removeLinesWithPrefix(adaptedShape);
        var expected = "\n<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://shaclshapes.org/confidence> 1E0 ;\n" + //problem with , in double
                "  <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "  <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "  <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "  <http://www.w3.org/ns/shacl#path> rdfs:label ." +
                "\n";
        System.out.println(expected);
        System.out.println(shapeWithoutPrefix);
        assertEquals("Finished shapes do not match",expected.replace("\r\n", "\n"), shapeWithoutPrefix.replace("\r\n", "\n"));
    }

    @Test
    public void testDeleteWhenOnlyOneOrItemIsLeftFullTest() {
        prepareTest();

        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();

        var nodeShapes = qbParser.shapesExtractor.getNodeShapes();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.setNodeShapes(nodeShapes);

        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film-v4labelgenreoneoritem", extractedShapes.getNodeShapes());
        RegexUtils regexUtils = new RegexUtils();

        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_v4labelgenreoneoritem.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;
        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

        assertTrue("Files are not equal", compareFiles(copiedFile,
                "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL_v4labelgenreoneoritem.ttl"));
    }

    @Test
    public void deleteFromMultipleOrItems() {
        //SHACL should not contain or list anymore, when only one item is left
        RegexUtils regexUtils = new RegexUtils();
        String shape = "<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "<http://www.w3.org/ns/shacl#or> ( [\n" +
                "   <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "   <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "] [\n" +
                "   <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "   <http://www.w3.org/ns/shacl#datatype> rdf:integer ;\n" +
                "] [\n" +
                "   <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "   <http://www.w3.org/ns/shacl#datatype> xsd:string ;\n" +
                "] ) ;\n" +
                "<http://www.w3.org/ns/shacl#path> rdfs:label .";
        ShaclOrListItem orListItem = new ShaclOrListItem(SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#Literal"),null,SimpleValueFactory.getInstance().createIRI("xsd:string"));
        String deletedShape = regexUtils.deleteShaclOrItemWithIriFromString(orListItem, shape, false);
        var expected = "<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "<http://www.w3.org/ns/shacl#or> ( [\n" +
                "   <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "   <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "] [\n" +
                "   <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "   <http://www.w3.org/ns/shacl#datatype> rdf:integer ;\n" +
                "] ) ;\n" +
                "<http://www.w3.org/ns/shacl#path> rdfs:label .";
        System.out.println(expected);
        System.out.println(deletedShape);
        assertEquals("Finished shapes do not match",expected, deletedShape);
    }

    //with new QSE-shactor version

    private void prepareTest() {
        Main.setResourcesPathForJar(resourcesPath);
        Main.setOutputFilePathForJar(outputPath);
        Main.setPruningThresholds(pruningThresholds);
        Main.annotateSupportConfidence = "true";
        Main.datasetName = firstVersionName;
        Main.configPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\emptyconfig.txt"; //avoid exceptions in QSE
    }

    @Test
    public void testDeletePropertyShapeWithIRI() {
        prepareTest();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();

        var nodeShapes = qbParser.shapesExtractor.getNodeShapes();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.setNodeShapes(nodeShapes);

        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film-NoSubPropertyOfSymmetricProperty", extractedShapes.getNodeShapes());
        RegexUtils regexUtils = new RegexUtils();

        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;

        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

        assertTrue("Files are not equal", compareFiles(copiedFile,
                "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl"));
    }

    @Test
    public void testDeletePropertyShapeWithLiteral() {
        prepareTest();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();

        var nodeShapes = qbParser.shapesExtractor.getNodeShapes();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.setNodeShapes(nodeShapes);

        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "Film-NoGender", extractedShapes.getNodeShapes());
        RegexUtils regexUtils = new RegexUtils();

        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_noGender.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;

        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

        assertTrue("Files are not equal", compareFiles(copiedFile,
                "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACLNoGender.ttl"));
    }

    @Test
    public void testDeleteNoFilmStudio() {
        prepareTest();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();

        var nodeShapes = qbParser.shapesExtractor.getNodeShapes();
        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.setNodeShapes(nodeShapes);

        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film3", extractedShapes.getNodeShapes());
        RegexUtils regexUtils = new RegexUtils();

        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_NoFilmStudio.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        extractedShapes.fileContentPath = copiedFile;

        var content = regexUtils.deleteFromFileWhereSupportIsZero(extractedShapes);
        regexUtils.saveStringAsFile(content, copiedFile);

        assertTrue("Files are not equal", compareFiles(copiedFile,
                "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACLNoFilmStudio.ttl"));
    }

    public static boolean compareFiles(String filePath1, String filePath2) {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {

            String line1 = "";
            String line2 = "";

            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                if (!line1.trim().equals(line2.trim())) {
                    return false;
                }
            }

            return (line1 == null || line1.isEmpty()) && (line2 == null || line2.isEmpty());

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
