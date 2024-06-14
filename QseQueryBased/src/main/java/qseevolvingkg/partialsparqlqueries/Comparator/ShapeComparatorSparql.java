package qseevolvingkg.partialsparqlqueries.Comparator;

import cs.Main;
import cs.qse.common.ExperimentsUtil;
import cs.qse.common.structure.NS;
import cs.qse.querybased.nonsampling.QbParser;
import cs.utils.Constants;
import org.jetbrains.annotations.NotNull;
import qseevolvingkg.partialsparqlqueries.Utils.ConfigManager;
import qseevolvingkg.partialsparqlqueries.ShapeObjects.ExtractedShapes;
import qseevolvingkg.partialsparqlqueries.Utils.GraphDbUtils;
import qseevolvingkg.partialsparqlqueries.Utils.RegexUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

public class ShapeComparatorSparql {
    String graphDbUrl;
    String dataSetName1;
    String dataSetName2;
    String logFilePath;
    List<NS> firstNodeShapes;
    String shapePath1;
    public String outputPath;

    public ComparisonDiff doFullComparison(String threshold) {
        ComparisonDiff comparisonDiff = prepareQSE(threshold);

        //First Run
        runQse1(comparisonDiff);

        //Check shapes with SPARQL
        doComparisonSparql(firstNodeShapes, shapePath1, comparisonDiff);

        return comparisonDiff;
    }

    public ComparisonDiff doComparison(String threshold, ShapeComparatorQSE shapeComparatorQSE) {
        ComparisonDiff comparisonDiff = prepareQSE(threshold);
        comparisonDiff.durationQse1 = shapeComparatorQSE.comparisonDiff.durationQse1;

        //Check shapes with SPARQL
        doComparisonSparql(shapeComparatorQSE.firstNodeShapes, shapeComparatorQSE.shapePath1, comparisonDiff);

        return comparisonDiff;
    }

    public ShapeComparatorSparql(String graphDbUrl, String dataSetName1, String dataSetName2, String logFilePath) {
        this.graphDbUrl = graphDbUrl;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath+dataSetName1+"_"+dataSetName2+ File.separator;
        this.outputPath = System.getProperty("user.dir")+ File.separator + "Output" + File.separator;
    }

    private void runQse1(ComparisonDiff comparisonDiff) {
        Main.datasetName = dataSetName1;
        Main.setOutputFilePathForJar(outputPath+dataSetName1+File.separator);

        Instant startQSE1 = Instant.now();
        QbParser qbParser = new QbParser(100, Constants.RDF_TYPE, graphDbUrl, dataSetName1);
        qbParser.run();
        Instant endQSE1 = Instant.now();
        comparisonDiff.durationQse1 = Duration.between(startQSE1, endQSE1);

        firstNodeShapes = qbParser.shapesExtractor.getNodeShapes();
        shapePath1 = qbParser.shapesExtractor.getOutputFileAddress();
    }

    @NotNull
    private static ComparisonDiff prepareQSE(String threshold) {
        Main.setResourcesPathForJar(ConfigManager.getRelativeResourcesPathFromQse());
        Main.annotateSupportConfidence = "true";
        Main.setPruningThresholds(threshold);
        File currentDir = new File(System.getProperty("user.dir"));
        File emptyConfig = new File(currentDir, "src/test/expected_test_results/emptyconfig.txt");
        Main.configPath = emptyConfig.getAbsolutePath(); //avoid exceptions in QSE
        return new ComparisonDiff();
    }

    private void doComparisonSparql(List<NS> firstNodeShapes, String shapePath1, ComparisonDiff comparisonDiff) {
        Instant startSparql = Instant.now();
        ExtractedShapes extractedShapes1 = new ExtractedShapes();
        extractedShapes1.setNodeShapes(firstNodeShapes);
        ExtractedShapes extractedShapes2 = new ExtractedShapes();
        extractedShapes2.setNodeShapes(firstNodeShapes);
        HashMap<Double, List<Integer>> pruningThresholds = ExperimentsUtil.getSupportConfRange();
        extractedShapes2.support = pruningThresholds.entrySet().iterator().next().getValue().get(0);
        extractedShapes2.confidence = pruningThresholds.keySet().iterator().next();

        extractedShapes1.fileContentPath = shapePath1;

        GraphDbUtils.checkShapesInNewGraph(graphDbUrl, this.dataSetName2, extractedShapes2.getNodeShapes());

        Path parentDir = Paths.get(extractedShapes1.fileContentPath).getParent();
        var copiedFile = parentDir.resolve(dataSetName2+"_QSEQueryBased.ttl").toString();
        RegexUtils.copyFile(extractedShapes1.fileContentPath, copiedFile);
        extractedShapes2.fileContentPath = copiedFile;

        var content = RegexUtils.deleteFromFileWithPruning(extractedShapes2, comparisonDiff);
        RegexUtils.saveStringAsFile(content, copiedFile);
        Instant endSparql = Instant.now();
        comparisonDiff.durationSecondStep = Duration.between(startSparql, endSparql);

        Instant startComparison = Instant.now();
        extractedShapes1.getFileAsString();
        extractedShapes2.getFileAsString();
        ComparatorUtils.getEditedNodeShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        ComparatorUtils.getEditedPropertyShapes(comparisonDiff, extractedShapes1, extractedShapes2, firstNodeShapes);
        Instant endComparison = Instant.now();
        comparisonDiff.durationComparison = Duration.between(startComparison, endComparison);

        comparisonDiff.durationTotal = comparisonDiff.durationQse1.plus(comparisonDiff.durationSecondStep).plus(comparisonDiff.durationComparison);
        ComparatorUtils.exportComparisonToFile(logFilePath+"Sparql", comparisonDiff.toString());
    }
}
