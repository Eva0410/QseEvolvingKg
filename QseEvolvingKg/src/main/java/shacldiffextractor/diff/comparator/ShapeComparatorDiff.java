package shacldiffextractor.diff.comparator;

import cs.qse.common.ExperimentsUtil;
import cs.qse.common.structure.NS;
import shape_comparator.data.ExtractedShapes;
import shacldiffextractor.diff.DiffManager;
import sparqlshapechecker.comparator.ComparatorUtils;
import sparqlshapechecker.comparator.ComparisonDiff;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShapeComparatorDiff {
    public String filePath1;
    public String filePath2;
    public String dataSetName1;
    public String dataSetName2;
    String logFilePath;
    List<NS> firstNodeShapes;
    String shapePath1;
    public String outputPath;
    public String filePathAdded;
    public String filePathDeleted;

    public void doFullComparisonForMultipleVersions(String threshold, String[] dataSetsToCheck) {
        ShapeComparatorQseFileBased.prepareQse(threshold);
        ComparisonDiff comparisonDiff = new ComparisonDiff();

        //First Run
        var diffManager = runQse1(comparisonDiff, threshold);
        var durationQse1 = comparisonDiff.durationQse1;
        for (var dataSet : dataSetsToCheck) {
            this.dataSetName2 = dataSet;
            doComparisonDiff(firstNodeShapes, shapePath1, comparisonDiff, diffManager);

            comparisonDiff = new ComparisonDiff();
            comparisonDiff.durationQse1 = durationQse1;
        }
    }

    public ComparisonDiff doFullComparison(String threshold) {
        ComparisonDiff comparisonDiff = new ComparisonDiff();
        DiffManager diffManager = this.runQse1(comparisonDiff, threshold);
        this.doComparisonDiff(firstNodeShapes, shapePath1, comparisonDiff, diffManager);
        return comparisonDiff;
    }

    public ComparisonDiff doComparison(String threshold, ShapeComparatorQseFileBased shapeComparatorQseFileBased) {
        ShapeComparatorQseFileBased.prepareQse(threshold);
        ComparisonDiff comparisonDiff = new ComparisonDiff();
        comparisonDiff.durationQse1 = shapeComparatorQseFileBased.comparisonDiff.durationQse1;
        DiffManager diffManager = new DiffManager();
        diffManager.parser = shapeComparatorQseFileBased.parser;
        doComparisonDiff(shapeComparatorQseFileBased.firstNodeShapes, shapeComparatorQseFileBased.shapePath1, comparisonDiff, diffManager);

        return comparisonDiff;
    }

    public ShapeComparatorDiff(String filePath1, String filePath2, String filePathAdded, String filePathDeleted, String dataSetName1, String dataSetName2, String logFilePath) {
        this.filePathAdded = filePathAdded;
        this.filePathDeleted = filePathDeleted;
        this.filePath1 = filePath1;
        this.filePath2 = filePath2;
        this.dataSetName1 = dataSetName1;
        this.dataSetName2 = dataSetName2;
        this.logFilePath = logFilePath;
        this.outputPath = System.getProperty("user.dir")+ File.separator + "Output" + File.separator;
    }

    private DiffManager runQse1(ComparisonDiff comparisonDiff, String threshold) {
        ShapeComparatorQseFileBased comparatorQseFileBased = new ShapeComparatorQseFileBased(filePath1, filePath2, dataSetName1, dataSetName2, logFilePath);
        var comparisonDiffFirst = comparatorQseFileBased.runQseFirstTime(threshold, Path.of("Output", "DiffExtractor"));

        comparisonDiff.durationQse1 = comparisonDiffFirst.durationQse1;
        firstNodeShapes = comparatorQseFileBased.firstNodeShapes;
        shapePath1 = comparatorQseFileBased.shapePath1;
        DiffManager diffManager = new DiffManager();
        diffManager.parser = comparatorQseFileBased.parser;
        return diffManager;
    }

    private void doComparisonDiff(List<NS> firstNodeShapes, String shapePath1, ComparisonDiff comparisonDiff, DiffManager diffManager) {
        Instant startSparql = Instant.now();
        ExtractedShapes extractedShapes2 = diffManager.executeQseDiff(filePathAdded, filePathDeleted);

        HashMap<Double, List<Integer>> pruningThresholds = ExperimentsUtil.getSupportConfRange();
        extractedShapes2.support = pruningThresholds.entrySet().iterator().next().getValue().get(0); //todo
        extractedShapes2.confidence = pruningThresholds.keySet().iterator().next();
        Instant endSparql = Instant.now();
        comparisonDiff.durationSecondStep = Duration.between(startSparql, endSparql);

        Instant startComparison = Instant.now();

        ExtractedShapes extractedShapes1 = new ExtractedShapes();
        extractedShapes1.fileContentPath = shapePath1;
        extractedShapes1.setNodeShapes(firstNodeShapes,false);
        comparisonDiff.deletedNodeShapes = new ArrayList<>(diffManager.diffShapeGenerator.deletedNodeShapeNames.stream().distinct().toList());
        comparisonDiff.deletedPropertyShapes = new ArrayList<>(diffManager.diffShapeGenerator.deletedPropertyShapeNames.stream().distinct().toList());
        comparisonDiff.addedNodeShapes = new ArrayList<>(diffManager.diffShapeGenerator.addedNodeShapeNames.stream().distinct().toList());
        comparisonDiff.addedPropertyShapes = new ArrayList<>(diffManager.diffShapeGenerator.addedPropertyShapeNames.stream().distinct().toList());
        comparisonDiff.editedNodeShapes = new ArrayList<>(diffManager.diffShapeGenerator.editedNodeShapes.stream().distinct().toList());
        comparisonDiff.editedPropertyShapes = new ArrayList<>(diffManager.diffShapeGenerator.editPropertyShpaes.stream().distinct().toList());

        extractedShapes1.getFileAsString(true);
        extractedShapes2.getFileAsString(true);
        Instant endComparison = Instant.now();
        comparisonDiff.durationComparison = Duration.between(startComparison, endComparison);

        comparisonDiff.durationTotal = comparisonDiff.durationQse1.plus(comparisonDiff.durationSecondStep).plus(comparisonDiff.durationComparison);
        ComparatorUtils.exportComparisonToFile(logFilePath+dataSetName1+"_"+dataSetName2+ File.separator+"Diff", comparisonDiff.toStringAll());
    }
}
