package qseevolvingkg.partialsparqlqueries;

import java.util.ArrayList;
import java.util.List;

public class MetaComparator {
    public ComparisonDiff diffQse;
    public ComparisonDiff diffSparql;

    public String compare() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n==== Comparison of Compare-Methods ====\n");
        sb.append("=== Deleted Node Shapes ===\n");
        List<String> uniqueToFirstList = getDifference(diffQse.deletedNodeShapes, diffSparql.deletedNodeShapes);
        List<String> uniqueToSecondList = getDifference(diffSparql.deletedNodeShapes, diffQse.deletedNodeShapes);
        appendUniqueNamesToStringBuilder(uniqueToFirstList, sb, uniqueToSecondList);

        sb.append("=== Deleted Property Shapes ===\n");
        uniqueToFirstList = getDifference(diffQse.deletedPropertyShapes, diffSparql.deletedPropertyShapes);
        uniqueToSecondList = getDifference(diffSparql.deletedPropertyShapes, diffQse.deletedPropertyShapes);
        appendUniqueNamesToStringBuilder(uniqueToFirstList, sb, uniqueToSecondList);

        sb.append("=== Edited Node Shape Names ===\n");
        if(diffQse.editedNodeShapes != null && diffSparql.editedNodeShapes != null) {
            var uniqueToFirstListObjects = getDifferenceBetweenObjectLists(diffQse.editedNodeShapes, diffSparql.editedNodeShapes);
            var uniqueToSecondListObjects = getDifferenceBetweenObjectLists(diffSparql.editedNodeShapes, diffQse.editedNodeShapes);
            appendUniqeNamesToStringBuilder(uniqueToFirstListObjects, sb, uniqueToSecondListObjects);
        }
        sb.append("=== Edited Property Shape Names ===\n");
        if(diffQse.editedPropertyShapes != null && diffSparql.editedPropertyShapes != null) {
            var uniqueToFirstListObjects = getDifferenceBetweenObjectLists(diffQse.editedPropertyShapes, diffSparql.editedPropertyShapes);
            var uniqueToSecondListObjects = getDifferenceBetweenObjectLists(diffSparql.editedPropertyShapes, diffQse.editedPropertyShapes);
            appendUniqeNamesToStringBuilder(uniqueToFirstListObjects, sb, uniqueToSecondListObjects);
        }
        sb.append("Execution Time QSE Total: ").append(diffQse.durationTotal.getSeconds()).append(" seconds, Execution Time Sparql Total: ").append(diffSparql.durationTotal.getSeconds()).append(" seconds");
        return sb.toString();
    }

    private static void appendUniqeNamesToStringBuilder(List<EditedShapesComparisonObject> uniqueToFirstListObjects, StringBuilder sb, List<EditedShapesComparisonObject> uniqueToSecondListObjects) {
        if (!uniqueToFirstListObjects.isEmpty())
            sb.append("== Unique in QSE-Comparison ==\n");
        uniqueToFirstListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
        if (!uniqueToSecondListObjects.isEmpty())
            sb.append("== Unique in Sparql-Comparison ==\n");
        uniqueToSecondListObjects.forEach(s -> sb.append(s.shapeName).append("\n"));
    }

    private static void appendUniqueNamesToStringBuilder(List<String> uniqueToFirstList, StringBuilder sb, List<String> uniqueToSecondList) {
        if (!uniqueToFirstList.isEmpty())
            sb.append("== Unique in QSE-Comparison ==\n");
        uniqueToFirstList.forEach(s -> sb.append(s).append("\n"));
        if (!uniqueToSecondList.isEmpty())
            sb.append("== Unique in Sparql-Comparison ==\n");
        uniqueToSecondList.forEach(s -> sb.append(s).append("\n"));
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
