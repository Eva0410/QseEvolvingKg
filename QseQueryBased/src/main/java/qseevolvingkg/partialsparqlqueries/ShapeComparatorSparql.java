package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.NS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ShapeComparatorSparql {
    String graphDbUrl;
    String dataSetName1;
    String dataSetName2;
    String logFilePath;
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/qse/src/main/resources";
    List<NS> firstNodeShapes;
    String shapePath1;
    ExtractedShapes extractedShapes2;
    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/";
//    public static final String pruningThresholds = "{(-1,0)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}

    public ShapeComparatorSparql(String graphDbUrl, String dataSetName1, String dataSetName2, String logFilePath) {
        this.graphDbUrl = graphDbUrl;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath+dataSetName1+"_"+dataSetName2+ File.separator;
    }

    public ComparisonDiff doComparison() {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.annotateSupportConfidence = "true";
        cs.Main.setPruningThresholds("{(-1,0)}"); //for default shapes
        cs.Main.configPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\emptyconfig.txt"; //avoid exceptions in QSE
        ComparisonDiff comparisonDiff = new ComparisonDiff();

        //First Run
        cs.Main.datasetName = dataSetName1;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName1+"/");

        Instant startQSE1 = Instant.now();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName1);
        qbParser.run();
        Instant endQSE1 = Instant.now();
        Duration durationQSE1 = Duration.between(startQSE1, endQSE1);
        comparisonDiff.durationQse1 = durationQSE1;

        firstNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath1 = qbParser.shapesExtractor.getOutputFileAddress();

        //Check shapes with SPARQL
        Instant startSparql = Instant.now();
        ExtractedShapes extractedShapes1 = new ExtractedShapes();
        extractedShapes1.setNodeShapes(firstNodeShapes);
        ExtractedShapes extractedShapes2 = new ExtractedShapes();
        extractedShapes2.setNodeShapes(firstNodeShapes);
        extractedShapes1.fileContentPath = shapePath1;

        GraphDbUtils.checkShapesInNewGraph(graphDbUrl, this.dataSetName2, extractedShapes2.getNodeShapes());

        Path parentDir = Paths.get(extractedShapes1.getFileContentPath()).getParent();
        var copiedFile = parentDir.resolve(dataSetName2+"_QSEQueryBased.ttl").toString();
        RegexUtils.copyFile(extractedShapes1.fileContentPath, copiedFile);
        extractedShapes2.fileContentPath = copiedFile;

        var content = RegexUtils.deleteFromFileWhereSupportIsZero(extractedShapes2, comparisonDiff);
        RegexUtils.saveStringAsFile(content, copiedFile);
        Instant endSparql = Instant.now();
        Duration durationSparql = Duration.between(startSparql, endSparql);
        comparisonDiff.durationSecondStep = durationSparql;

        Instant startComparison = Instant.now();
        extractedShapes1.getFileAsString();
        extractedShapes2.getFileAsString();
        ComparatorUtils.getEditedNodeShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        ComparatorUtils.getEditedPropertyShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        Instant endComparison = Instant.now();
        Duration durationComparison = Duration.between(startComparison, endComparison);
        comparisonDiff.durationComparison = durationComparison;

        Duration totalDuration = durationQSE1.plus(durationSparql).plus(durationComparison);
        comparisonDiff.durationTotal = totalDuration;
        System.out.println(comparisonDiff);
        ComparatorUtils.exportComparisonToFile(logFilePath+"Sparql", this.toString());
        return comparisonDiff;
    }
}
