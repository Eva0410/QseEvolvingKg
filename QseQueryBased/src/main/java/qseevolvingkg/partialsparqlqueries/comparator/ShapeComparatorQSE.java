package qseevolvingkg.partialsparqlqueries.comparator;

import cs.Main;
import cs.qse.common.structure.NS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import qseevolvingkg.partialsparqlqueries.utils.ConfigManager;
import qseevolvingkg.partialsparqlqueries.shapeobjects.ExtractedShapes;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ShapeComparatorQSE {
    String graphDbUrl;
    String dataSetName1;
    String dataSetName2;
    String logFilePath;
    public List<NS> firstNodeShapes;
    List<NS> secondNodeShapes;
    public String shapePath1;
    String shapePath2;
    public String outputPath;
    public ComparisonDiff comparisonDiff;

    public ShapeComparatorQSE(String graphDbUrl, String dataSetName1, String dataSetName2, String logFilePath) {
        this.graphDbUrl = graphDbUrl;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath+dataSetName1+"_"+dataSetName2+ File.separator;
        this.outputPath = System.getProperty("user.dir")+ File.separator + "Output" + File.separator;
    }

    public ComparisonDiff doComparison(String threshold) {
        cs.Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds(threshold);
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE

        //First Run
        Main.datasetName = dataSetName1;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName1+File.separator);
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
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName2+File.separator);

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

        comparisonDiff.durationTotal = durationQSE1.plus(durationQSE2).plus(durationComparison);
        ComparatorUtils.exportComparisonToFile(logFilePath+"QSE", comparisonDiff.toString());
        this.comparisonDiff = comparisonDiff;
        return comparisonDiff;
    }

    private void getDeletedPropertyShapes(ComparisonDiff comparisonDiff) {
        var propertyShapes1 = new java.util.ArrayList<>(firstNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString())).distinct().toList());
        var propertyShapes2 = secondNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString())).distinct().toList();
        propertyShapes1.removeAll(propertyShapes2);
        comparisonDiff.deletedPropertyShapes = propertyShapes1;
    }

    private void getDeletedNodeShapes(ComparisonDiff comparisonDiff) {
        var firstShapesCopied = new java.util.ArrayList<>(firstNodeShapes.stream().map(ns -> ns.getIri().toString()).distinct().toList());
        var secondShapesCopied = secondNodeShapes.stream().map(ns -> ns.getIri().toString()).distinct().toList();
        firstShapesCopied.removeAll(secondShapesCopied);
        comparisonDiff.deletedNodeShapes = firstShapesCopied;
    }
}
