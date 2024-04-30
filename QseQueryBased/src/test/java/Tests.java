import org.junit.Test;
import qseevolvingkg.partialsparqlqueries.GraphDbUtils;
import qseevolvingkg.partialsparqlqueries.RegexUtils;

public class Tests {
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/qse/src/main/resources";
    public static final String firstVersionName = "film";

    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
    public static final String pruningThresholds = "{(-1,10)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";

    @Test
    public void test() {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.setOutputFilePathForJar(outputPath);
        cs.Main.setPruningThresholds(pruningThresholds);
        cs.Main.annotateSupportConfidence = "true";
        cs.Main.datasetName = firstVersionName;

//        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
//        qbParser.run();
//        var localPath = qbParser.dbDefaultConnectionString;
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        result.forEach(r -> System.out.println(r));
        System.out.println("new version");
        graphDbUtils.checkNodeShapesInNewGraph(graphDbUrl, "film3", result);
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
        graphDbUtils.checkNodeShapesInNewGraph(graphDbUrl, "film3", result);
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
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.setOutputFilePathForJar(outputPath);
        cs.Main.setPruningThresholds(pruningThresholds);
        cs.Main.annotateSupportConfidence = "true";
        cs.Main.datasetName = firstVersionName + "_cloned";
        var goalRepo = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\film\\secondVersion";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        graphDbUtils.constructDefaultShapes(goalRepo);
    }
}
