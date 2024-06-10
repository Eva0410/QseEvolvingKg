package qseevolvingkg.partialsparqlqueries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetaComparator {
    public ComparisonDiff diffQse;
    public ComparisonDiff diffSparql;

    public String compare() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n==== Comparison of Compare-Methods ====\n");
        sb.append("=== Deleted Node Shapes ===\n");
        List<String> uniqueToFirstList = getDifference(diffQse.deletedNodeShapes, diffSparql.deletedNodeShapes);
        List<String> uniqueToSecondList = getDifference(diffSparql.deletedNodeShapes, diffQse.deletedNodeShapes);
        if (!uniqueToFirstList.isEmpty())
            sb.append("== Unique in QSE-Comparison ==\n");
        uniqueToFirstList.forEach(s -> sb.append(s).append("\n"));
        if (!uniqueToSecondList.isEmpty())
            sb.append("== Unique in Sparql-Comparison ==\n");
        uniqueToSecondList.forEach(s -> sb.append(s).append("\n"));

        sb.append("=== Deleted Property Shapes ===\n");
        uniqueToFirstList = getDifference(diffQse.deletedPropertShapes, diffSparql.deletedPropertShapes);
        uniqueToSecondList = getDifference(diffSparql.deletedPropertShapes, diffQse.deletedPropertShapes);
        if (!uniqueToFirstList.isEmpty())
            sb.append("== Unique in QSE-Comparison ==\n");
        uniqueToFirstList.forEach(s -> sb.append(s).append("\n"));
        if (!uniqueToSecondList.isEmpty())
            sb.append("== Unique in Sparql-Comparison ==\n");
        uniqueToSecondList.forEach(s -> sb.append(s).append("\n"));

        sb.append("=== Edited Node Shape Names ===\n");
        if(diffQse.editedNodeShapes != null && diffSparql.editedNodeShapes != null) {
            var uniqueToFirstListObjects = getDifferenceBetweenObjectLists(diffQse.editedNodeShapes, diffSparql.editedNodeShapes);
            var uniqueToSecondListObjects = getDifferenceBetweenObjectLists(diffSparql.editedNodeShapes, diffQse.editedNodeShapes);
            if (!uniqueToFirstListObjects.isEmpty())
                sb.append("== Unique in QSE-Comparison ==\n");
            uniqueToFirstListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
            if (!uniqueToSecondListObjects.isEmpty())
                sb.append("== Unique in Sparql-Comparison ==\n");
            uniqueToSecondListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
        }
        sb.append("=== Edited Property Shape Names ===\n");
        if(diffQse.editedPropertyShapes != null && diffSparql.editedPropertyShapes != null) {
            var uniqueToFirstListObjects = getDifferenceBetweenObjectLists(diffQse.editedPropertyShapes, diffSparql.editedPropertyShapes);
            var uniqueToSecondListObjects = getDifferenceBetweenObjectLists(diffSparql.editedPropertyShapes, diffQse.editedPropertyShapes);
            if (!uniqueToFirstListObjects.isEmpty())
                sb.append("== Unique in QSE-Comparison ==\n");
            uniqueToFirstListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
            if (!uniqueToSecondListObjects.isEmpty())
                sb.append("== Unique in Sparql-Comparison ==\n");
            uniqueToSecondListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
        }
        sb.append("Execution Time QSE Total: " + diffQse.durationTotal.getSeconds() + " seconds, Execution Time Sparql Total: " + diffSparql.durationTotal.getSeconds() + " seconds");
        return sb.toString();
    }
    public static List<String> getDifference(List<String> list1, List<String> list2) {
        List<String> difference = new ArrayList<>(list1);
        difference.removeAll(list2);
        return difference;
    }

    public static List<EditedShapesComparisonObject> getDifferenceBetweenObjectLists(List<EditedShapesComparisonObject> list1, List<EditedShapesComparisonObject> list2) {
        var difference = new ArrayList<>(list1);
        difference.removeAll(list2);
        return difference;
    }
}
