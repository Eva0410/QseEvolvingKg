package qseevolvingkgwebapp.services;

import qseevolvingkgwebapp.data.NodeShape;

import java.util.Objects;

public class ComparisionTreeViewItem {

    public ComparisionTreeViewItem(NodeShape ns) {
        shapeName = ns.getIri().getLocalName();
    }

    public ComparisionTreeViewItem() {

    }

    public ComparisionTreeViewItem(String name) {
        shapeName = name;
    }
    String shapeName;

    public String getShapeName() {
        return shapeName;
    }

    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComparisionTreeViewItem)) return false;
        ComparisionTreeViewItem that = (ComparisionTreeViewItem) o;
        return Objects.equals(shapeName, that.shapeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeName);
    }
}
