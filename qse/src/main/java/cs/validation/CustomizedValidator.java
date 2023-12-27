//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.validation;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.VLib;
import org.apache.jena.shacl.validation.ValidationListener;
import org.apache.jena.shacl.validation.event.ValidationEvent;

public class CustomizedValidator {
    public CustomizedValidator() {
    }

    private static Node createResource(String uri) {
        return NodeFactory.createURI(uri);
    }

    private static void parseShapeGraph(String inputDataFilePath, String inputSHACLFilePath, String outputSHACLFilePath, String outputCSVFilePath) {
        try {
            Graph shapesGraph = RDFDataMgr.loadGraph(inputSHACLFilePath);
            Shapes shapes = Shapes.parse(shapesGraph);
            shapes.getShapeMap().forEach((n, s) -> {
                System.out.println("" + n + " -> " + s);
            });
            Iterator var6 = shapes.getTargetShapes().iterator();

            while(var6.hasNext()) {
                Shape shape = (Shape)var6.next();
                shape.getPropertyShapes().forEach((propertyShape) -> {
                    propertyShape.getConstraints().forEach((constraint) -> {
                        System.out.println();
                    });
                });
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    private static void validateWithListener(String inputDataFilePath, String inputSHACLFilePath, String outputSHACLFilePath, String outputCSVFilePath) {
        try {
            Graph shapesGraph = RDFDataMgr.loadGraph(inputSHACLFilePath);
            Graph dataGraph = RDFDataMgr.loadGraph(inputDataFilePath);
            Shapes shapes = Shapes.parse(shapesGraph);
            RecordingValidationListener listener = new RecordingValidationListener();
            ValidationContext vCtx = ValidationContext.create(shapes, dataGraph, listener);
            Iterator var9 = shapes.getTargetShapes().iterator();

            while(var9.hasNext()) {
                Shape shape = (Shape)var9.next();
                Collection<Node> focusNodes = VLib.focusNodes(dataGraph, shape);
                Iterator var12 = focusNodes.iterator();

                while(var12.hasNext()) {
                    Node focusNode = (Node)var12.next();
                    vCtx.setVerbose(true);
                    VLib.validateShape(vCtx, dataGraph, shape, focusNode);
                }
            }

            System.out.println(" --- Writing to file ---  ");
            OutputStream out = new FileOutputStream(outputSHACLFilePath, false);
            RDFDataMgr.write(out, vCtx.generateReport().getModel(), Lang.TTL);
        } catch (Exception var14) {
            var14.printStackTrace();
        }

    }

}
class RecordingValidationListener implements ValidationListener {
    private final Set<ValidationEvent> events = new HashSet<>();

    @Override
    public void onValidationEvent(ValidationEvent e) {
        events.add(e);
    }

    public Set<ValidationEvent> getEvents() {
        return events;
    }
}
