import cs.Main;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.apache.jena.base.Sys;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.GraphDbUtils;
import qseevolvingkg.partialsparqlqueries.RegexUtils;
import qseevolvingkg.partialsparqlqueries.ShaclOrListItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tests {
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/qse/src/main/resources";
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
        var localPath = qbParser.dbDefaultConnectionString;
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
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film3", result);
        result.forEach(r -> System.out.println(r));
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_copy.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        var content = regexUtils.deleteFromFileWhereSupportIsZero(copiedFile, result);
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
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "Film-NoGender", result);
        result.forEach(r -> System.out.println(r));
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_copyPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        var content = regexUtils.deleteFromFileWhereSupportIsZero(copiedFile, result);
        regexUtils.saveStringAsFile(content, copiedFile);
    }

    @Test
    public void deleteWithPropertyShapeIriInNewRepo() throws IOException {
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        graphDbUtils.checkShapesInNewGraph(graphDbUrl, "film-NoSubPropertyOfSymmetricProperty", result);
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        var content = regexUtils.deleteFromFileWhereSupportIsZero(copiedFile, result);
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
        RegexUtils regexUtils = new RegexUtils();
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        var copiedFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL_subPropertySymmetricPropertyShape.ttl";
        regexUtils.copyFile(sourceFile, copiedFile);
        var content = regexUtils.deleteFromFileWhereSupportIsZero(copiedFile, result);
        regexUtils.saveStringAsFile(content, copiedFile);
    }

    @Test
    public void deleteFromOrItems() {
        var sourceFile = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\film_QSE_FULL_SHACL.ttl";
        RegexUtils regexUtils = new RegexUtils();
        String shape = regexUtils.getShapeAsString("http://shaclshapes.org/labelGenreShapeProperty", regexUtils.getFileAsString(sourceFile));
        ShaclOrListItem orListItem = new ShaclOrListItem(SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#Literal"),null,SimpleValueFactory.getInstance().createIRI("xsd:string"));
        String deletedShape = regexUtils.deleteShaclOrItemWithIriFromString(orListItem, shape, false);
        var expected = "\n<http://shaclshapes.org/labelGenreShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#or> ([\n" +
                "    <http://shaclshapes.org/confidence> 0E0 ;\n" +
                "    <http://shaclshapes.org/support> \"1\"^^xsd:int ;\n" +
                "    <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "    <http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "  ] ) ;\n" +
                "  <http://www.w3.org/ns/shacl#path> rdfs:label .\n";
        System.out.println(expected);
        System.out.println(deletedShape);
        assertEquals("Finished shapes do not match",expected, deletedShape);
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
