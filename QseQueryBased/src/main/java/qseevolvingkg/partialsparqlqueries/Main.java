package qseevolvingkg.partialsparqlqueries;

import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

public class Main {
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/qse/src/main/resources";
    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/";
    public static final String pruningThresholds = "{(0.1,100)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}
    public static final String graphDbUrl = "http://localhost:7201/";
    public static final String firstVersionName = "Bear-B";
    public static void main(String[] args) {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.setOutputFilePathForJar(outputPath);
        cs.Main.setPruningThresholds(pruningThresholds);
        cs.Main.annotateSupportConfidence = "true";

        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, firstVersionName);
        qbParser.run();
        var filePath = qbParser.prettyFormattedShaclFilePath;


//        //TODO do RQ4
//        RepositoryManager repositoryManager = new RemoteRepositoryManager("http://localhost:7201/");
//        var repo = repositoryManager.getRepository("Bear-B");
//        var all = repositoryManager.getAllRepositories();
//        repo.init();
//        System.out.println("Hello world!");
    }
}