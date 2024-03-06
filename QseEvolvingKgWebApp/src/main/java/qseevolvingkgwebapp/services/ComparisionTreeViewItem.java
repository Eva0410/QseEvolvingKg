package qseevolvingkgwebapp.services;

import qseevolvingkgwebapp.data.NodeShape;
import qseevolvingkgwebapp.data.PropertyShape;

import java.util.HashMap;

//represents one line in the tree view. This can be either be a line of 2+ node shapes, or of 2+ property shapes
//node or property shapes have the same shapename
public class ComparisionTreeViewItem {

    public ComparisionTreeViewItem() {
    }

    String shapeName;
    HashMap<Long,NodeShape> nodeShapeList = new HashMap<>();
    HashMap<Long,PropertyShape> propertyShapeList = new HashMap<>();
    Boolean shapesEqual = null;

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

    public Boolean areShapesEqual() {
        return shapesEqual;
    }

    public void setShapesEqual(Boolean shapesEqual) {
        this.shapesEqual = shapesEqual;
    }
}
