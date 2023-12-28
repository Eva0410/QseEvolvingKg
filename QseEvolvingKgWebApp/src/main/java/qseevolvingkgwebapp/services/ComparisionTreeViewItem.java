package qseevolvingkgwebapp.services;

import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.NodeShape;
import qseevolvingkgwebapp.data.PropertyShape;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ComparisionTreeViewItem {

    public ComparisionTreeViewItem(NodeShape ns) {
        shapeName = ns.getIri().getLocalName();
    }

    public ComparisionTreeViewItem(PropertyShape ps) {
        shapeName = ps.getIri().getLocalName();
    }

    public ComparisionTreeViewItem() {

    }

    public ComparisionTreeViewItem(String name) {
        shapeName = name;
    }
    String shapeName;
    HashMap<Long,NodeShape> nodeShapeList = new HashMap<>();
    HashMap<Long,PropertyShape> propertyShapeList = new HashMap<>();

    public void addNodeShape(NodeShape ns, Long extractedShapesId) {
        if(nodeShapeList.size() == 0)
            shapeName = ns.getIri().getLocalName();
        nodeShapeList.put(extractedShapesId, ns);
    }

    public void addPropertyShape(PropertyShape ps, Long extractedShapesId) {
        if(nodeShapeList.size() == 0)
            shapeName = ps.getIri().getLocalName();
        propertyShapeList.put(extractedShapesId, ps);
    }

    public boolean isNodeShapeLine() {
        return nodeShapeList.size() > 0;
    }

    public HashMap<Long, NodeShape> getNodeShapeList() {
        return nodeShapeList;
    }


    public HashMap<Long, PropertyShape> getPropertyShapeList() {
        return propertyShapeList;
    }

    public String getShapeName() {
        return shapeName;
    }

    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }

}
