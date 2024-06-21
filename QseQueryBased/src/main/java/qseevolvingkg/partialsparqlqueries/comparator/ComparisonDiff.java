package qseevolvingkg.partialsparqlqueries.comparator;

import java.time.Duration;
import java.util.ArrayList;

public class ComparisonDiff {
    public ArrayList<String> deletedNodeShapes;
    public ArrayList<String> deletedPropertyShapes;
    public ArrayList<EditedShapesComparisonObject> editedNodeShapes;
    public ArrayList<EditedShapesComparisonObject> editedPropertyShapes;
    Duration durationQse1;
    Duration durationSecondStep;
    Duration durationComparison;
    Duration durationTotal;

    public ComparisonDiff() {
        deletedNodeShapes = new ArrayList<>();
        deletedPropertyShapes = new ArrayList<>();
        editedNodeShapes = new ArrayList<>();
        editedPropertyShapes = new ArrayList<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Deleted Node Shapes ===\n");
        for (String str : deletedNodeShapes) {
            sb.append(str).append("\n");
        }
        sb.append("=== Deleted Property Shapes ===\n");
        for (String str : deletedPropertyShapes) {
            sb.append(str).append("\n");
        }
        sb.append("=== Edited Node Shape Names ===\n");
        if(editedNodeShapes != null) {
            for (String str : editedNodeShapes.stream().map(e -> e.shapeName).toList()) {
                sb.append(str).append("\n");
            }
        }
        sb.append("=== Edited Property Shape Names ===\n");
        if(editedPropertyShapes != null) {
            for (String str : editedPropertyShapes.stream().map(e -> e.shapeName).toList()) {
                sb.append(str).append("\n");
            }
        }
        sb.append("=== Edited Node Shapes ===\n");
        if(editedNodeShapes != null) {
            for (String str : editedNodeShapes.stream().map(EditedShapesComparisonObject::toString).toList()) {
                sb.append(str).append("\n");
            }
        }
        sb.append("=== Edited Property Shapes ===\n");
        if(editedPropertyShapes != null) {
            for (String str : editedPropertyShapes.stream().map(EditedShapesComparisonObject::toString).toList()) {
                sb.append(str).append("\n");
            }
        }
        sb.append("\nExecution Time QSE 1: ").append(durationQse1.getSeconds()).append(" seconds");
        sb.append("\nExecution Time Second Step (QSE or Sparql-Script): ").append(durationSecondStep.getSeconds()).append(" seconds");
        sb.append("\nExecution Time Comparison: ").append(durationComparison.getSeconds()).append(" seconds");
        sb.append("\nExecution Time Total: ").append(durationTotal.getSeconds()).append(" seconds");

        return sb.toString();
    }
}
