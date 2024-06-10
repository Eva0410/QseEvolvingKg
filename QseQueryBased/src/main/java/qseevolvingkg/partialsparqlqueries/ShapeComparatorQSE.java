package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.common.structure.NS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ShapeComparatorQSE {
    String graphDbUrl;
    String dataSetName1;
    String dataSetName2;
    String logFilePath;
    public static final String resourcesPath = "/Users/evapu/Documents/GitHub/qse/src/main/resources";
    List<NS> firstNodeShapes;
    List<NS> secondNodeShapes;
    String shapePath1;
    String shapePath2;
    public static final String outputPath = "/Users/evapu/Documents/GitHub/QseEvolvingKg/QSEQueryBased/Output/";
//    public static final String pruningThresholds = "{(-1,0)}"; //only set one threshold - {(<confidence 10% is 0.1>,<support>)}

    public ShapeComparatorQSE(String graphDbUrl, String dataSetName1, String dataSetName2, String logFilePath) {
        this.graphDbUrl = graphDbUrl;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath+dataSetName1+"_"+dataSetName2+ File.separator;
    }

    public ComparisonDiff doComparison() {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(-1,0)}"); //for default shapes
        Main.configPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\emptyconfig.txt"; //avoid exceptions in QSE

        //First Run
        Main.datasetName = dataSetName1;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName1+"/");
        ComparisonDiff comparisonDiff = new ComparisonDiff();

        Instant startQSE1 = Instant.now();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName1);
        qbParser.run();
        Instant endQSE1 = Instant.now();
        Duration durationQSE1 = Duration.between(startQSE1, endQSE1);
        comparisonDiff.durationQse1 = durationQSE1;

        firstNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath1 = qbParser.shapesExtractor.getOutputFileAddress();

        //Second Run
        Main.datasetName = dataSetName2;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName2+"/");

        Instant startQSE2 = Instant.now();
        qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName2);
        qbParser.run();
        Instant endQSE2 = Instant.now();
        Duration durationQSE2 = Duration.between(startQSE2, endQSE2);
        comparisonDiff.durationSecondStep = durationQSE2;

        secondNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath2 = qbParser.shapesExtractor.getOutputFileAddress();

        Instant startComparison = Instant.now();
        getDeletedNodeShapes(comparisonDiff);
        getDeletedPropertyShapes(comparisonDiff);
        ExtractedShapes extractedShapes1 = new ExtractedShapes();
        ExtractedShapes extractedShapes2 = new ExtractedShapes();
        extractedShapes1.fileContentPath = shapePath1;
        extractedShapes2.fileContentPath = shapePath2;
        extractedShapes1.getFileAsString();
        extractedShapes2.getFileAsString();
        ComparatorUtils.getEditedNodeShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        ComparatorUtils.getEditedPropertyShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        Instant endComparison = Instant.now();
        Duration durationComparison = Duration.between(startComparison, endComparison);
        comparisonDiff.durationComparison = durationComparison;

        Duration totalDuration = durationQSE1.plus(durationQSE2).plus(durationComparison);
        comparisonDiff.durationTotal = totalDuration;
        System.out.println(comparisonDiff);
        ComparatorUtils.exportComparisonToFile(logFilePath+"QSE", this.toString());
        return comparisonDiff;
    }

    private void getDeletedPropertyShapes(ComparisonDiff comparisonDiff) {
        var propertyShapes1 = new java.util.ArrayList<>(firstNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString())).toList());
        var propertyShapes2 = secondNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString())).toList();
        propertyShapes1.removeAll(propertyShapes2);
        comparisonDiff.deletedPropertShapes = propertyShapes1;
    }

    private void getDeletedNodeShapes(ComparisonDiff comparisonDiff) {
        var firstShapesCopied = new java.util.ArrayList<>(firstNodeShapes.stream().map(ns -> ns.getIri().toString()).toList());
        var secondShapesCopied = secondNodeShapes.stream().map(ns -> ns.getIri().toString()).toList();
        firstShapesCopied.removeAll(secondShapesCopied);
        comparisonDiff.deletedNodeShapes = firstShapesCopied;
    }
}
