package qseevolvingkg.partialsparqlqueries;

import com.ontotext.trree.query.functions.math.E;
import cs.Main;
import cs.qse.common.structure.NS;
import cs.qse.common.structure.PS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.apache.zookeeper.server.quorum.CommitProcessor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ShapeComparatorQSETwice {
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

    public ShapeComparatorQSETwice(String graphDbUrl, String dataSetName1, String dataSetName2, String logFilePath) {
        this.graphDbUrl = graphDbUrl;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath;
    }

    public ComparisonDiff doComparison() {
        cs.Main.setResourcesPathForJar(resourcesPath);
        cs.Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds("{(-1,0)}"); //for default shapes
        Main.configPath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QSEQueryBased\\src\\test\\expected_test_results\\emptyconfig.txt"; //avoid exceptions in QSE

        //First Run
        Main.datasetName = dataSetName1;
        cs.Main.setOutputFilePathForJar(outputPath+dataSetName1+"/");

        Instant startQSE1 = Instant.now();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName1);
        qbParser.run();
        Instant endQSE1 = Instant.now();
        Duration durationQSE1 = Duration.between(startQSE1, endQSE1);
        System.out.println("Execution Time QSE 1: " + durationQSE1.getSeconds() + " seconds"); //todo save to log

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
        System.out.println("Execution Time QSE 2: " + durationQSE2.getSeconds() + " seconds"); //todo save to log

        secondNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath2 = qbParser.shapesExtractor.getOutputFileAddress();

        Instant startComparison = Instant.now();
        ComparisonDiff comparisonDiff = new ComparisonDiff();
        getDeletedNodeShapes(comparisonDiff);
        getDeletedPropertyShapes(comparisonDiff);
        var file1String = RegexUtils.getFileAsString(shapePath1);
        var file2String = RegexUtils.getFileAsString(shapePath2);
        getEditedNodeShapes(comparisonDiff, file1String, file2String);
        getEditedPropertyShapes(comparisonDiff, file1String, file2String);
        Instant endComparison = Instant.now();
        Duration durationComparison = Duration.between(startComparison, endComparison);
        System.out.println("Execution Time Comparison: " + durationComparison.getSeconds() + " seconds"); //todo save to log

        System.out.println(comparisonDiff.toString());
        Duration totalDuration = durationQSE1.plus(durationQSE2).plus(durationComparison);
        System.out.println("Total Execution Time: " + totalDuration.getSeconds() + " seconds"); //todo save to log
        return comparisonDiff;
    }

    private void getEditedPropertyShapes(ComparisonDiff comparisonDiff,  String file1String, String file2String) {
        var propertyShapesToCheck = firstNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString()))
                .filter(ps -> !comparisonDiff.deletedPropertShapes.contains(ps)).toList();
        var editedShapes = generateEditeShapesObjects(propertyShapesToCheck, file1String, file2String);
        comparisonDiff.editedNodeShapes = editedShapes;
    }

    private void getEditedNodeShapes(ComparisonDiff comparisonDiff, String file1String, String file2String) {
        var nodeShapesToCheck = firstNodeShapes.stream().filter(ns -> !comparisonDiff.deletedNodeShapes.contains(ns.getIri().toString())).map(ns -> ns.getIri().toString()).toList();
        var editedShapes = generateEditeShapesObjects(nodeShapesToCheck, file1String, file2String);
        comparisonDiff.editedNodeShapes = editedShapes;
    }

    private static ArrayList<EditedShapesComparisonObject> generateEditeShapesObjects(List<String> shapesToCheck, String file1String, String file2String) {
        var editedShapesComparisonObjects = new ArrayList<EditedShapesComparisonObject>();
        for(var shape : shapesToCheck) {
            EditedShapesComparisonObject editedShapesComparisonObject = new EditedShapesComparisonObject();
            editedShapesComparisonObject.shapeName = shape;
            var shapeString1 = RegexUtils.getShapeAsString(shape, file1String);
            var shapeString2 = RegexUtils.getShapeAsString(shape, file2String);
            if(!shapeString1.equals(shapeString2)) {
                editedShapesComparisonObject.shapeAsTextNew = shapeString2;
                editedShapesComparisonObject.shapeAsTextOld = shapeString1;
                editedShapesComparisonObjects.add(editedShapesComparisonObject);
            }
        }
        return editedShapesComparisonObjects;
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
