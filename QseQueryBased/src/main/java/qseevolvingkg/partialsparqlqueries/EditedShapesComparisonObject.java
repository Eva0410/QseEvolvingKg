package qseevolvingkg.partialsparqlqueries;

import java.util.Objects;

public class EditedShapesComparisonObject{
    String shapeName;
    String shapeAsTextNew;
    String shapeAsTextOld;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(shapeName);
        sb.append("\nNew Version: \n").append(shapeAsTextNew);
        sb.append("\n");
        sb.append("\nOld Version: \n").append(shapeAsTextOld);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditedShapesComparisonObject that = (EditedShapesComparisonObject) o;
        return Objects.equals(shapeName, that.shapeName) && Objects.equals(shapeAsTextNew, that.shapeAsTextNew) && Objects.equals(shapeAsTextOld, that.shapeAsTextOld);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeName);
    }
}
