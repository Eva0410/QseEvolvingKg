package qseevolvingkg.partialsparqlqueries.diff;

import cs.Main;
import cs.qse.filebased.Parser;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;
import qseevolvingkg.partialsparqlqueries.utils.RegexUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiffManager {
    public static String instanceTypeProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
    public Parser parser;
    public DiffExtractor diffExtractor;
    public DiffShapeGenerator diffShapeGenerator;
    int support = 0;
    double confidence = 0.0;


    public ExtractedShapes run(String dataSetName, String filePath, String filePathAdded, String filePathDeleted) {
        this.executeQse(filePath, dataSetName);
        return this.executeQseDiff(filePathAdded, filePathDeleted);
    }

    public void executeQse(String filePath, String dataSetName) {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{("+confidence+","+support+")}");
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        Main.saveCountInPropertyData=true;
        Path basePath = Paths.get( "Output", "DiffExtractor", dataSetName, "Original");
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cs.Main.setOutputFilePathForJar(basePath.toAbsolutePath()+File.separator);

        clearOutputDirectory(basePath.getParent());
        Main.datasetName = dataSetName;
        parser = new Parser(filePath, 3, 10, instanceTypeProperty);
        runParser(parser);
    }

    public static void clearOutputDirectory(Path outputPath) {
        try {
            Files.walk(outputPath)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runParser(Parser parser) {
        parser.entityExtraction();
        parser.entityConstraintsExtraction();
        parser.computeSupportConfidence();
        parser.extractSHACLShapes(false, Main.qseFromSpecificClasses);
    }
    public ExtractedShapes executeQseDiff(String filePathAdded, String filePathDeleted) {
        diffExtractor = new DiffExtractor(filePathAdded, filePathDeleted,parser,support, confidence);
        diffExtractor.extractFromFile();

        diffShapeGenerator = new DiffShapeGenerator(diffExtractor);
        diffShapeGenerator.computeDeletedShapes();
        diffShapeGenerator.generateDiffMap();
        diffShapeGenerator.generateDiffShapesWithQse();

        var mergedFilePath = diffShapeGenerator.mergeAddedShapesToOrginialFileAsString();
        var fileAsString = RegexUtils.getFileAsString(mergedFilePath);
        var fileWithDeletedShapes = diffShapeGenerator.deleteShapesFromFile(fileAsString);
        var filePath = Paths.get(Main.outputFilePath).getParent() + File.separator + Main.datasetName+"InclDiff.ttl";
        RegexUtils.saveStringAsFile(fileWithDeletedShapes, filePath);
        var extractedShapes = new ExtractedShapes();
        extractedShapes.fileContentPath = filePath;
        extractedShapes.nodeShapes = diffShapeGenerator.resultExtractedShapes.getNodeShapes();
        return extractedShapes;
    }
}
