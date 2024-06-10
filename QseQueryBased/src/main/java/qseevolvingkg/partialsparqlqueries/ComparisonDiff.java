package qseevolvingkg.partialsparqlqueries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ComparisonDiff {
    public ArrayList<String> deletedNodeShapes;
    public ArrayList<String> deletedPropertShapes;
    public ArrayList<EditedShapesComparisonObject> editedNodeShapes;
    public ArrayList<EditedShapesComparisonObject> editedPropertyShapes;

    public ComparisonDiff() {
        deletedNodeShapes = new ArrayList<>();
        deletedPropertShapes = new ArrayList<>();
        editedNodeShapes = new ArrayList<>();
        editedPropertyShapes = new ArrayList<>();
    }

    public void exportComparisonToFile(String filePath) {
        try {
            String fileName = filePath+ File.separator+"Comparison" + LocalDateTime.now();
            File outputFile = new File(fileName);
            if(outputFile.createNewFile()) {
                FileWriter writer = new FileWriter(fileName);
                writer.write(this.toString());
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Deleted Node Shapes ===\n");
        for (String str : deletedNodeShapes) {
            sb.append(str).append("\n");
        }
        sb.append("=== Deleted Property Shapes ===\n");
        for (String str : deletedPropertShapes) {
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
        return sb.toString();
    }
}
