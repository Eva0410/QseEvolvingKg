package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.NS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ComparatorUtils {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void getEditedPropertyShapes(ComparisonDiff comparisonDiff, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2, List<NS> firstNodeShapes) {
        var propertyShapesToCheck = firstNodeShapes.stream().flatMap(ns -> ns.getPropertyShapes().stream().map(ps -> ps.getIri().toString()))
                .filter(ps -> !comparisonDiff.deletedPropertShapes.contains(ps)).toList();
        var editedShapes = generateEditedShapesObjects(propertyShapesToCheck, extractedShapes1, extractedShapes2);
        comparisonDiff.editedPropertyShapes = editedShapes;
    }

    public static void getEditedNodeShapes(ComparisonDiff comparisonDiff, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2, List<NS> firstNodeShapes) {
        var nodeShapesToCheck = firstNodeShapes.stream().filter(ns -> !comparisonDiff.deletedNodeShapes.contains(ns.getIri().toString())).map(ns -> ns.getIri().toString()).toList();
        var editedShapes = generateEditedShapesObjects(nodeShapesToCheck, extractedShapes1, extractedShapes2);
        comparisonDiff.editedNodeShapes = editedShapes;
    }

    private static ArrayList<EditedShapesComparisonObject> generateEditedShapesObjects(List<String> shapesToCheck, ExtractedShapes extractedShapes1, ExtractedShapes extractedShapes2) {
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

    public static void exportComparisonToFile(String filePath, String content) {
        try {
            String fileName = filePath+"Comparison.txt";
            File outputFile = new File(fileName);
            File parentDir = outputFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    return;
                }
            }
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.close();
            LOGGER.info("Saved file " + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
