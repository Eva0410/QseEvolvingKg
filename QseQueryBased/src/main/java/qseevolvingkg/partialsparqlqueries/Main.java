package qseevolvingkg.partialsparqlqueries;

public class Main {
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/qse/src/main/resources";
    public static final String firstVersionName = "lubm-mini";

    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/"+firstVersionName+"/";
    //QSE QueryBases does not calculate confidence, therefore it is always 0 and filtering works with > 0 -> filter to -1
    public static final String pruningThresholds = "{(-1,10)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";
    public static void main(String[] args) {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.setOutputFilePathForJar(outputPath);
        cs.Main.setPruningThresholds(pruningThresholds);
        cs.Main.annotateSupportConfidence = "true";
        cs.Main.datasetName = firstVersionName;

//        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
//        qbParser.run();
//        var filePath = qbParser.prettyFormattedShaclFilePath;
//        var localPath = qbParser.dbDefaultConnectionString;
//        C:\Users\evapu\Documents\GitHub\QseEvolvingKg\QSEQueryBased\Output\lubm-mini\db_default
        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\lubm-mini\\db_default";
        GraphDbUtils graphDbUtils = new GraphDbUtils();
        var result = graphDbUtils.getNodeShapesWithTargetClassFromRepo(localPath);
        result.forEach(r -> System.out.println(r));

//        //TODO do RQ4
//        RepositoryManager repositoryManager = new RemoteRepositoryManager("http://localhost:7201/");
//        var repo = repositoryManager.getRepository("Bear-B");
//        var all = repositoryManager.getAllRepositories();
//        repo.init();
//        System.out.println("Hello world!");


//        var localPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\Output\\lubm-mini\\test";
//
//        Repository db = new SailRepository(new NativeStore(new File(localPath))); // Create a new Repository.
//
//        try (RepositoryConnection conn = db.getConnection()) { // Open a connection to the database
//            Model m = null;
//            ModelBuilder b = new ModelBuilder();
//            b.subject("http://example.org/resource1")
//                    .add("http://example.org/property", "Value1")
//                    .subject("http://example.org/resource2")
//                    .add("http://example.org/property", "Value2");
//            m = b.build();
//            conn.add(m);
//        } finally {
//            db.shutDown();// before our program exits, make sure the database is properly shut down.
//        }
//
//        GraphDbUtils graphDbUtils = new GraphDbUtils();
//        graphDbUtils.getNodeShapesWithTargetClassFromFile(localPath);
    }
}