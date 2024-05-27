//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cs.qse.common;

import cs.Main;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.filebased.SupportConfidence;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple3;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class MinCardinalityExperiment {
    Model model = null;
    ModelBuilder builder = null;
    StringEncoder stringEncoder;
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Integer> classInstanceCount;
    ValueFactory factory = SimpleValueFactory.getInstance();
    String logfileAddress;

    public MinCardinalityExperiment(StringEncoder stringEncoder, Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport, Map<Integer, Integer> classInstanceCount) {
        this.logfileAddress = Constants.EXPERIMENTS_RESULT_MIN_CARD;
        this.stringEncoder = stringEncoder;
        this.builder = new ModelBuilder();
        this.shapeTripletSupport = shapeTripletSupport;
        this.classInstanceCount = classInstanceCount;
        this.builder.setNamespace("shape", Constants.SHAPES_NAMESPACE);
    }

    public void constructDefaultShapes(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes) {
        this.model = null;
        this.builder = new ModelBuilder();
        this.model = this.builder.build();
        this.model.addAll(this.constructShapeWithoutPruning(classToPropWithObjTypes));
        System.out.println("MODEL:: DEFAULT - SIZE: " + this.model.size());
        HashMap<String, String> currentShapesModelStats = this.computeShapeStatistics(this.model);
        StringBuilder header = new StringBuilder("DATASET,Confidence,Support,");
        StringBuilder log = new StringBuilder(Main.datasetName + ", > 1.0%, > 1.0,");

        StringBuilder var10002;
        for(Iterator var5 = currentShapesModelStats.entrySet().iterator(); var5.hasNext(); header = new StringBuilder("" + var10002 + ",")) {
            Map.Entry<String, String> entry = (Map.Entry)var5.next();
            String v = (String)entry.getValue();
            var10002 = log.append(v);
            log = new StringBuilder("" + var10002 + ",");
            var10002 = header.append((String)entry.getKey());
        }

        FilesUtil.writeToFileInAppendMode(header.toString(), this.logfileAddress);
        FilesUtil.writeToFileInAppendMode(log.toString(), this.logfileAddress);
    }

    public void constructPrunedShapes(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Double confidence, Integer support) {
        this.model = null;
        this.builder = new ModelBuilder();
        this.model = this.builder.build();
        this.model.addAll(this.constructShapesWithPruning(classToPropWithObjTypes, confidence, support));
        PrintStream var10000 = System.out;
        int var10001 = this.model.size();
        var10000.println("MODEL:: CUSTOM - SIZE: " + var10001 + " | PARAMS: " + confidence * 100.0 + " - " + support);
        HashMap<String, String> currentShapesModelStats = this.computeShapeStatistics(this.model);
        String var10002 = Main.datasetName;
        StringBuilder log = new StringBuilder(var10002 + ", > " + confidence * 100.0 + "%, > " + support + ",");

        StringBuilder var9;
        for(Iterator var6 = currentShapesModelStats.entrySet().iterator(); var6.hasNext(); log = new StringBuilder("" + var9 + ",")) {
            Map.Entry<String, String> entry = (Map.Entry)var6.next();
            String v = (String)entry.getValue();
            var9 = log.append(v);
        }

        FilesUtil.writeToFileInAppendMode(log.toString(), this.logfileAddress);
    }

    private Model constructShapeWithoutPruning(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes) {
        Model m = null;
        ModelBuilder b = new ModelBuilder();
        classToPropWithObjTypes.forEach((classEncodedLabel, propToObjectType) -> {
            IRI subj = this.factory.createIRI(this.stringEncoder.decode(classEncodedLabel));
            String nodeShape = "shape:" + subj.getLocalName() + "Shape";
            b.subject(nodeShape).add(RDF.TYPE, SHACL.NODE_SHAPE).add(SHACL.TARGET_CLASS, subj).add(SHACL.IGNORED_PROPERTIES, RDF.TYPE).add(SHACL.CLOSED, false);
            if (propToObjectType != null) {
                this.constructNodePropertyShapes(b, subj, nodeShape, propToObjectType);
            }

        });
        m = b.build();
        return m;
    }

    private Model constructShapesWithPruning(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Double confidence, Integer support) {
        Model m = null;
        ModelBuilder b = new ModelBuilder();
        classToPropWithObjTypes.forEach((classEncodedLabel, propToObjectType) -> {
            IRI subj = this.factory.createIRI(this.stringEncoder.decode(classEncodedLabel));
            String nodeShape = "shape:" + subj.getLocalName() + "Shape";
            b.subject(nodeShape).add(RDF.TYPE, SHACL.NODE_SHAPE).add(SHACL.TARGET_CLASS, subj).add(SHACL.IGNORED_PROPERTIES, RDF.TYPE).add(SHACL.CLOSED, false);
            if (propToObjectType != null) {
                this.constructNodePropertyShapes(b, subj, nodeShape, propToObjectType, confidence, support);
            }

        });
        m = b.build();
        return m;
    }

    private void constructNodePropertyShapes(ModelBuilder b, IRI subj, String nodeShape, Map<Integer, Set<Integer>> propToObjectTypesLocal) {
        propToObjectTypesLocal.forEach((prop, propObjectTypes) -> {
            IRI property = this.factory.createIRI(this.stringEncoder.decode(prop));
            ValueFactory var10000 = this.factory;
            String var10001 = property.getLocalName();
            IRI propShape = var10000.createIRI("sh:" + var10001 + subj.getLocalName() + "ShapeProperty");
            b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            propObjectTypes.forEach((encodedObjectType) -> {
                Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.stringEncoder.encode(subj.stringValue()), prop, encodedObjectType);
                if (this.shapeTripletSupport.containsKey(tuple3) && ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.stringEncoder.encode(subj.stringValue())))) {
                    b.subject(propShape).add(SHACL.MIN_COUNT, 1);
                }

                String objectType = this.stringEncoder.decode(encodedObjectType);
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
                        b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                    }
                } else {
                    b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                }

            });
        });
    }

    private void constructNodePropertyShapes(ModelBuilder b, IRI subj, String nodeShape, Map<Integer, Set<Integer>> propToObjectTypesLocal, Double confidence, Integer support) {
        propToObjectTypesLocal.forEach((prop, propObjectTypes) -> {
            IRI property = this.factory.createIRI(this.stringEncoder.decode(prop));
            ValueFactory var10000 = this.factory;
            String var10001 = property.getLocalName();
            IRI propShape = var10000.createIRI("sh:" + var10001 + subj.getLocalName() + "ShapeProperty");
            b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            propObjectTypes.forEach((encodedObjectType) -> {
                Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.stringEncoder.encode(subj.stringValue()), prop, encodedObjectType);
                if (this.shapeTripletSupport.containsKey(tuple3) && ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport() > support && ((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getConfidence() > confidence) {
                    b.subject(propShape).add(SHACL.MIN_COUNT, 1);
                }

                String objectType = this.stringEncoder.decode(encodedObjectType);
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
                        b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                    }
                } else {
                    b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                }

            });
        });
    }

    private HashMap<Integer, HashSet<Integer>> performNodeShapePropPruning(Integer classEncodedLabel, HashMap<Integer, HashSet<Integer>> propToObjectType, Double confidence, Integer support) {
        HashMap<Integer, HashSet<Integer>> propToObjectTypesLocal = new HashMap();
        propToObjectType.forEach((prop, propObjectTypes) -> {
            HashSet<Integer> objTypesSet = new HashSet();
            propObjectTypes.forEach((encodedObjectType) -> {
                Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(classEncodedLabel, prop, encodedObjectType);
                if (this.shapeTripletSupport.containsKey(tuple3)) {
                    SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                    if (support == 1) {
                        if (sc.getConfidence() > confidence && sc.getSupport() >= support) {
                            objTypesSet.add(encodedObjectType);
                        }
                    } else if (sc.getConfidence() > confidence && sc.getSupport() > support) {
                        objTypesSet.add(encodedObjectType);
                    }
                }

            });
            if (objTypesSet.size() != 0) {
                propToObjectTypesLocal.put(prop, objTypesSet);
            }

        });
        return propToObjectTypesLocal;
    }

    public HashMap<String, String> computeShapeStatistics(Model currentModel) {
        HashMap<String, String> shapesStats = new HashMap();
        Repository db = new SailRepository(new MemoryStore());
        db.init();

        try {
            RepositoryConnection conn = db.getConnection();

            try {
                conn.add(currentModel, new Resource[0]);

                int i;
                String type;
                TupleQuery query;
                Value queryOutput;
                Literal literalCount;
                for(i = 1; i <= 5; ++i) {
                    type = "count";
                    query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
                    queryOutput = this.executeQuery(query, type);
                    if (queryOutput != null && queryOutput.isLiteral()) {
                        literalCount = (Literal)queryOutput;
                        shapesStats.put((String)ExperimentsUtil.getCsvHeader().get(i), literalCount.stringValue());
                    }
                }

                for(i = 1; i <= 4; ++i) {
                    type = "avg";
                    query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
                    queryOutput = this.executeQuery(query, type);
                    if (queryOutput != null && queryOutput.isLiteral()) {
                        literalCount = (Literal)queryOutput;
                        shapesStats.put((String)ExperimentsUtil.getAverageHeader().get(i), literalCount.stringValue());
                    }

                    type = "max";
                    query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
                    queryOutput = this.executeQuery(query, type);
                    if (queryOutput != null && queryOutput.isLiteral()) {
                        literalCount = (Literal)queryOutput;
                        shapesStats.put((String)ExperimentsUtil.getMaxHeader().get(i), literalCount.stringValue());
                    }

                    type = "min";
                    query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
                    queryOutput = this.executeQuery(query, type);
                    if (queryOutput != null && queryOutput.isLiteral()) {
                        literalCount = (Literal)queryOutput;
                        shapesStats.put((String)ExperimentsUtil.getMinHeader().get(i), literalCount.stringValue());
                    }
                }
            } catch (Throwable var15) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var14) {
                        var15.addSuppressed(var14);
                    }
                }

                throw var15;
            }

            if (conn != null) {
                conn.close();
            }
        } finally {
            db.shutDown();
        }

        return shapesStats;
    }

    public void writeModelToFile(String fileIdentifier) {
        Path path = Paths.get(Main.datasetPath);
        String var10000 = FilenameUtils.removeExtension(path.getFileName().toString());
        String fileName = var10000 + "_" + fileIdentifier + "_SHACL.ttl";
        System.out.println("::: SHACLER ~ WRITING MODEL TO FILE: " + fileName);

        try {
            FileWriter fileWriter = new FileWriter(Main.outputFilePath + fileName, false);
            Rio.write(this.model, fileWriter, RDFFormat.TURTLE);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    private Value executeQuery(TupleQuery query, String bindingName) {
        Value queryOutput = null;

        try {
            TupleQueryResult result = query.evaluate();

            try {
                while(result.hasNext()) {
                    BindingSet solution = (BindingSet)result.next();
                    queryOutput = solution.getValue(bindingName);
                }
            } catch (Throwable var8) {
                if (result != null) {
                    try {
                        result.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (result != null) {
                result.close();
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return queryOutput;
    }
}
