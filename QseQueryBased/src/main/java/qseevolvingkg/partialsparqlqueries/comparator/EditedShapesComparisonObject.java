package qseevolvingkg.partialsparqlqueries.comparator;

import java.util.Objects;

public class EditedShapesComparisonObject{
    String shapeName;
    String shapeAsTextNew;
    String shapeAsTextOld;

    public String toString() {
        return "--- " + shapeName +
                "\nNew Version: \n" + shapeAsTextNew +
                "\n" +
                "\nOld Version: \n" + shapeAsTextOld +
                "\n";
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
