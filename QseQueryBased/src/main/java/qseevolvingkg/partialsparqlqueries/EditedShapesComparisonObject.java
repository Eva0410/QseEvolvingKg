package qseevolvingkg.partialsparqlqueries;

public class EditedShapesComparisonObject {
    String shapeName;
    String shapeAsTextNew;
    String shapeAsTextOld;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- " +shapeName);
        sb.append("\nNew Version: \n" +shapeAsTextNew);
        sb.append("\nOld Version: \n" +shapeAsTextOld);
        return sb.toString();
    }
}
