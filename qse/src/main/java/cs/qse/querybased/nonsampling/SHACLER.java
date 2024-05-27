//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.querybased.nonsampling;

import cs.Main;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.Tuple3;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.yars.nx.Node;

public class SHACLER {
    String classIRI;
    HashMap<Node, HashSet<String>> propToType = null;
    ValueFactory factory = SimpleValueFactory.getInstance();
    Model model = null;
    ModelBuilder builder = null;
    StringEncoder stringEncoder;
    HashMap<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    HashMap<Integer, Integer> classInstanceCount;
    String logfileAddress;

    public SHACLER() {
        this.logfileAddress = Main.outputFilePath + Main.datasetName + ".csv";
        this.builder = new ModelBuilder();
        this.builder.setNamespace("shape", Constants.SHAPES_NAMESPACE);
    }

    public SHACLER(StringEncoder stringEncoder, HashMap<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport, HashMap<Integer, Integer> classInstanceCount) {
        this.logfileAddress = Main.outputFilePath + Main.datasetName + ".csv";
        this.stringEncoder = stringEncoder;
        this.builder = new ModelBuilder();
        this.shapeTripletSupport = shapeTripletSupport;
        this.classInstanceCount = classInstanceCount;
        this.builder.setNamespace("shape", Constants.SHAPES_NAMESPACE);
    }

    public void setParams(Node classNode, HashMap<Node, HashSet<String>> propToType) {
        this.classIRI = classNode.getLabel();
        this.propToType = propToType;
    }

    public void setParams(String classLabel, HashMap<Node, HashSet<String>> propToType) {
        this.classIRI = classLabel;
        this.propToType = propToType;
    }

    public void constructShape() {
        Model m = null;
        ModelBuilder b = new ModelBuilder();
        IRI subj = this.factory.createIRI(this.classIRI);
        String nodeShape = "shape:" + subj.getLocalName() + "Shape";
        b.subject(nodeShape).add(RDF.TYPE, SHACL.NODE_SHAPE).add(SHACL.TARGET_CLASS, subj).add(SHACL.CLOSED, false);
        if (this.propToType != null) {
            this.propToType.forEach((prop, propObjectTypes) -> {
                IRI property = this.factory.createIRI(prop.getLabel());
                ValueFactory var10000 = this.factory;
                String var10001 = property.getLocalName();
                IRI propShape = var10000.createIRI("sh:" + var10001 + subj.getLocalName() + "ShapeProperty");
                b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
                b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property).add(SHACL.MIN_COUNT, 1);
                propObjectTypes.forEach((objectType) -> {
                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            objectTypeIri = this.factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.CLASS, objectTypeIri);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.DATATYPE, objectTypeIri);
                        }
                    } else {
                        b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                    }

                });
            });
        }

        m = b.build();
        this.model = this.builder.build();
        this.model.addAll(m);
    }

    public Statement createStatement(IRI s, IRI p, IRI o) {
        return this.factory.createStatement(s, p, o);
    }

    public void printModel() {
        Rio.write(this.model, System.out, RDFFormat.TURTLE);
    }

    public void writeModelToFile() {
        Path path = Paths.get(Main.datasetPath);
        String fileName = FilenameUtils.removeExtension(path.getFileName().toString()) + "_SHACL.ttl";
        System.out.println("::: SHACLER ~ WRITING MODEL TO FILE: " + fileName);

        try {
            FileWriter fileWriter = new FileWriter(Main.outputFilePath + fileName, false);
            Rio.write(this.model, fileWriter, RDFFormat.TURTLE);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public void printModel(Model m) {
        Rio.write(m, System.out, RDFFormat.TURTLE);
    }
}
