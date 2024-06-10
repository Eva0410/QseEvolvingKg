package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.common.structure.NS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.jgrapht.Graph;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
        this.logFilePath = logFilePath;
    }

    public ComparisonDiff doComparison() {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.annotateSupportConfidence = "true";
        cs.Main.setPruningThresholds("{(-1,0)}"); //for default shapes
        cs.Main.configPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\emptyconfig.txt"; //avoid exceptions in QSE

        //First Run
        cs.Main.datasetName = dataSetName1;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName1+"/");

        Instant startQSE1 = Instant.now();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName1);
        qbParser.run();
        Instant endQSE1 = Instant.now();
        Duration durationQSE1 = Duration.between(startQSE1, endQSE1);
        System.out.println("Execution Time QSE 1: " + durationQSE1.getSeconds() + " seconds"); //todo save to log

        firstNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath1 = qbParser.shapesExtractor.getOutputFileAddress();

        //Check shapes with SPARQL
        Instant startSparql = Instant.now();
        ExtractedShapes extractedShapes1 = new ExtractedShapes();
        extractedShapes1.setNodeShapes(firstNodeShapes);
        ExtractedShapes extractedShapes2 = new ExtractedShapes();
        extractedShapes2.setNodeShapes(firstNodeShapes);
        extractedShapes1.fileContentPath = shapePath1;

        ComparisonDiff comparisonDiff = new ComparisonDiff();
        GraphDbUtils.checkShapesInNewGraph(graphDbUrl, this.dataSetName2, extractedShapes2.getNodeShapes());

        Path parentDir = Paths.get(extractedShapes1.getFileContentPath()).getParent();
        var copiedFile = parentDir.resolve(dataSetName2+"_QSEQueryBased.ttl").toString();
        RegexUtils.copyFile(extractedShapes1.fileContentPath, copiedFile);
        extractedShapes2.fileContentPath = copiedFile;

        var content = RegexUtils.deleteFromFileWhereSupportIsZero(extractedShapes2, comparisonDiff);
        RegexUtils.saveStringAsFile(content, copiedFile);
        Instant endSparql = Instant.now();
        Duration durationSparql = Duration.between(startSparql, endSparql);
        System.out.println("Execution Time Sparql Comparison: " + durationSparql.getSeconds() + " seconds"); //todo save to log


        //todo optimize!
        Instant startComparison = Instant.now();
        extractedShapes1.getFileAsString();
        extractedShapes2.getFileAsString();
        getEditedNodeShapes(comparisonDiff, extractedShapes1, extractedShapes2);
        getEditedPropertyShapes(comparisonDiff, extractedShapes1, extractedShapes2);
        Instant endComparison = Instant.now();
        Duration durationComparison = Duration.between(startComparison, endComparison);
        System.out.println("Execution Time Comparison: " + durationComparison.getSeconds() + " seconds"); //todo save to log

        System.out.println(comparisonDiff.toString());
        Duration totalDuration = durationQSE1.plus(durationSparql).plus(durationComparison);
        System.out.println("Total Execution Time: " + totalDuration.getSeconds() + " seconds"); //todo save to log
        return comparisonDiff;
    }

    private void getEditedPropertyShapes(ComparisonDiff comparisonDiff, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2) {
        var propertyShapesToCheck = firstNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString()))
                .filter(ps -> !comparisonDiff.deletedPropertShapes.contains(ps)).toList();
        var editedShapes = generateEditeShapesObjects(propertyShapesToCheck, extractedShapes1, extractedShapes2);
        comparisonDiff.editedNodeShapes = editedShapes;
    }

    private void getEditedNodeShapes(ComparisonDiff comparisonDiff, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2) {
        var nodeShapesToCheck = firstNodeShapes.stream().filter(ns -> !comparisonDiff.deletedNodeShapes.contains(ns.getIri().toString())).map(ns -> ns.getIri().toString()).toList();
        var editedShapes = generateEditeShapesObjects(nodeShapesToCheck, extractedShapes1, extractedShapes2);
        comparisonDiff.editedNodeShapes = editedShapes;
    }

    private static ArrayList<EditedShapesComparisonObject> generateEditeShapesObjects(List<String> shapesToCheck, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2) {
        var editedShapesComparisonObjects = new ArrayList<EditedShapesComparisonObject>();
        for(var shape : shapesToCheck) {
            EditedShapesComparisonObject editedShapesComparisonObject = new EditedShapesComparisonObject();
            editedShapesComparisonObject.shapeName = shape;
            var shapeString1 = RegexUtils.getShapeAsStringFormatted(shape, extractedShapes1.fileAsString, extractedShapes1.prefixLines);
            var shapeString2 = RegexUtils.getShapeAsStringFormatted(shape, extractedShapes2.fileAsString, extractedShapes2.prefixLines);
            if(!shapeString1.equals(shapeString2)) {
                editedShapesComparisonObject.shapeAsTextNew = shapeString2;
                editedShapesComparisonObject.shapeAsTextOld = shapeString1;
                editedShapesComparisonObjects.add(editedShapesComparisonObject);
            }
        }
        return editedShapesComparisonObjects;
    }
}
