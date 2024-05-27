package cs.qse.common;

import com.google.common.collect.Lists;
import cs.Main;
import cs.qse.common.encoders.Encoder;
import cs.qse.common.structure.NS;
import cs.qse.common.structure.PS;
import cs.qse.common.structure.ShaclOrListItem;
import cs.qse.filebased.SupportConfidence;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import cs.utils.Tuple3;
import cs.utils.Utils;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

//Unfortunately WebApp does not work with current version of QSE. Therefore old version is saved here
public class ShapesExtractor_Old {
    ModelBuilder builder;
    Encoder encoder;
    Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport;
    Map<Integer, Integer> classInstanceCount;
    Map<Integer, Set<Integer>> propWithClassesHavingMaxCountOne;
    ValueFactory factory = SimpleValueFactory.getInstance();
    String logfileAddress;
    String outputFileAddress;
    Boolean isSamplingOn;
    Map<Integer, Integer> propCount;
    Map<Integer, Integer> sampledPropCount;
    Map<Integer, List<Integer>> sampledEntitiesPerClass;
    Map<Integer, List<Double>> supportToRelativeSupport;
    String typePredicate;
    RepositoryConnection defaultRepoConnection;
    RepositoryConnection prunedRepoConnection;
    List<NS> nodeShapes;
    NS currNodeShape;
    PS currPropertyShape;
    ShaclOrListItem currShaclOrListItem;
    Repository defaultShapesDb;
    HashMap<String, String> currentShapesModelStats;

    public HashMap<String, String> getCurrentShapesModelStats() {
        return this.currentShapesModelStats;
    }

    public List<NS> getNodeShapes() {
        return this.nodeShapes;
    }

    public Repository getDefaultShapesDb() {
        return this.defaultShapesDb;
    }

    public RepositoryConnection getDefaultRepoConnection() {
        return this.defaultRepoConnection;
    }

    public RepositoryConnection getPrunedRepoConnection() {
        return this.prunedRepoConnection;
    }

    public ShapesExtractor_Old() {
        this.logfileAddress = Constants.EXPERIMENTS_RESULT;
        this.outputFileAddress = "";
        this.isSamplingOn = false;
        this.supportToRelativeSupport = new HashMap();
    }

    public ShapesExtractor_Old(Encoder encoder, Map<Tuple3<Integer, Integer, Integer>, SupportConfidence> shapeTripletSupport, Map<Integer, Integer> classInstanceCount, String typePredicate) {
        this.logfileAddress = Constants.EXPERIMENTS_RESULT;
        this.outputFileAddress = "";
        this.isSamplingOn = false;
        this.supportToRelativeSupport = new HashMap();
        this.encoder = encoder;
        this.builder = new ModelBuilder();
        this.shapeTripletSupport = shapeTripletSupport;
        this.classInstanceCount = classInstanceCount;
        this.typePredicate = typePredicate;
        this.builder.setNamespace("shape", Constants.SHAPES_NAMESPACE);
    }

    public void constructDefaultShapes(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes) {
        File dbDir = new File(Main.outputFilePath + "db_default");
        performDirCheck(dbDir);
        this.defaultShapesDb = new SailRepository(new NativeStore(new File(dbDir.getAbsolutePath())));

        try {
            RepositoryConnection conn = this.defaultShapesDb.getConnection();

            try {
                this.defaultRepoConnection = conn;
                this.constructShapeWithoutPruning(classToPropWithObjTypes, conn);
                conn.setNamespace("shape", Constants.SHAPES_NAMESPACE);
                conn.setNamespace("shape", Constants.SHACL_NAMESPACE);
                PostConstraintsAnnotator pca = new PostConstraintsAnnotator(conn);
                pca.addShNodeConstraint();
                System.out.println("MODEL:: DEFAULT - SIZE: " + conn.size(new Resource[0]));
                this.currentShapesModelStats = this.computeShapeStatistics(conn);
                StringBuilder header = new StringBuilder("DATASET,Confidence,Support,");
                StringBuilder log = new StringBuilder(Main.datasetName + ", > 1.0%, > 1.0,");

                StringBuilder var10002;
                for(Iterator var7 = this.currentShapesModelStats.entrySet().iterator(); var7.hasNext(); header = new StringBuilder("" + var10002 + ",")) {
                    Map.Entry<String, String> entry = (Map.Entry)var7.next();
                    String v = (String)entry.getValue();
                    log = new StringBuilder(log.append(v).append(","));
                    var10002 = header.append((String)entry.getKey());
                }

                FilesUtil.writeToFileInAppendMode(header.toString(), this.logfileAddress);
                FilesUtil.writeToFileInAppendMode(log.toString(), this.logfileAddress);
                String outputFilePath = this.writeModelToFile("QSE_FULL", conn);
                this.prettyFormatTurtle(outputFilePath);
                FilesUtil.deleteFile(outputFilePath);
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
            this.defaultShapesDb.shutDown();
        }

    }

    private void constructShapeWithoutPruning(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, RepositoryConnection conn) {
        this.nodeShapes = new ArrayList();
        ArrayList classesList;
        if (classToPropWithObjTypes.size() > 10000) {
            classesList = new ArrayList(classToPropWithObjTypes.keySet());
            List<List<Integer>> classesPartition = Lists.partition(classesList, classToPropWithObjTypes.size() / 4);
            classesPartition.forEach((partition) -> {
                Model m = null;
                ModelBuilder b = new ModelBuilder();
                partition.forEach((encodedClassIRI) -> {
                    NS ns = new NS();
                    Map<Integer, Set<Integer>> propToObjectType = (Map)classToPropWithObjTypes.get(encodedClassIRI);
                    this.buildShapes(b, encodedClassIRI, propToObjectType, ns);
                    this.nodeShapes.add(ns);
                });
                m = b.build();
                conn.add(m, new Resource[0]);
            });
        } else {
            classesList = null;
            ModelBuilder b = new ModelBuilder();
            classToPropWithObjTypes.forEach((encodedClassIRI, propToObjectType) -> {
                NS ns = new NS();
                this.buildShapes(b, encodedClassIRI, propToObjectType, ns);
                this.nodeShapes.add(ns);
            });
            Model m = b.build();
            conn.add(m, new Resource[0]);
        }

    }

    private void buildShapes(ModelBuilder b, Integer encodedClassIRI, Map<Integer, Set<Integer>> propToObjectType, NS ns) {
        if (Utils.isValidIRI(this.encoder.decode(encodedClassIRI))) {
            IRI subj = this.factory.createIRI(this.encoder.decode(encodedClassIRI));
            String var10000 = Constants.SHAPES_NAMESPACE;
            String nodeShape = var10000 + subj.getLocalName() + "Shape";
            b.subject(nodeShape).add(RDF.TYPE, SHACL.NODE_SHAPE).add(SHACL.TARGET_CLASS, subj).add(Constants.SUPPORT, this.classInstanceCount.get(encodedClassIRI));
            ns.setIri(this.factory.createIRI(nodeShape));
            ns.setSupport((Integer)this.classInstanceCount.get(encodedClassIRI));
            ns.setTargetClass(subj);
            this.currNodeShape = ns;
            if (propToObjectType != null) {
                this.constructPropertyShapes(b, subj, encodedClassIRI, nodeShape, propToObjectType);
            }
        } else {
            Encoder var10001 = this.encoder;
            System.out.println("constructShapeWithoutPruning::INVALID SUBJECT IRI: " + var10001.decode(encodedClassIRI));
        }

    }

    public void constructPrunedShapes(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Double confidence, Integer support) {
        File dbDir = new File(Main.outputFilePath + "db_" + confidence + "_" + support);
        performDirCheck(dbDir);
        Repository db = new SailRepository(new NativeStore(new File(dbDir.getAbsolutePath())));
        RepositoryConnection conn = db.getConnection();

        try {
            this.prunedRepoConnection = conn;
            this.constructShapesWithPruning(classToPropWithObjTypes, confidence, support, conn);
            conn.setNamespace("shape", Constants.SHAPES_NAMESPACE);
            conn.setNamespace("shape", Constants.SHACL_NAMESPACE);
            PostConstraintsAnnotator pca_pruned = new PostConstraintsAnnotator(conn);
            pca_pruned.addShNodeConstraint();
            PrintStream var10000 = System.out;
            long var10001 = conn.size(new Resource[0]);
            var10000.println("MODEL:: CUSTOM - SIZE: " + var10001 + " | PARAMS: " + confidence * 100.0 + " - " + support);
            this.currentShapesModelStats = this.computeShapeStatistics(conn);
            String var10002 = Main.datasetName;
            StringBuilder log = new StringBuilder(var10002 + ", > " + confidence * 100.0 + "%, > " + support + ",");
            Iterator var9 = this.currentShapesModelStats.entrySet().iterator();

            while(true) {
                if (!var9.hasNext()) {
                    FilesUtil.writeToFileInAppendMode(log.toString(), this.logfileAddress);
                    String outputFilePath = this.writeModelToFile("QSE_" + confidence + "_" + support, conn);
                    this.prettyFormatTurtle(outputFilePath);
                    FilesUtil.deleteFile(outputFilePath);
                    break;
                }

                Map.Entry<String, String> entry = (Map.Entry)var9.next();
                String v = (String)entry.getValue();
                log = new StringBuilder(log.append(v).append(","));
            }
        } catch (Throwable var13) {
            if (conn != null) {
                try {
                    conn.close();
                    db.shutDown();
                } catch (Throwable var12) {
                    var13.addSuppressed(var12);
                }
            }

            throw var13;
        }

        if (conn != null) {
            conn.close();
            db.shutDown();
        }

    }

    private void constructShapesWithPruning(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes, Double confidence, Integer support, RepositoryConnection conn) {
        ArrayList classesList;
        if (classToPropWithObjTypes.size() > 10000) {
            classesList = new ArrayList(classToPropWithObjTypes.keySet());
            List<List<Integer>> classesPartition = Lists.partition(classesList, classToPropWithObjTypes.size() / 4);
            classesPartition.forEach((partition) -> {
                Model m = null;
                ModelBuilder b = new ModelBuilder();
                partition.forEach((encodedClassIRI) -> {
                    Map<Integer, Set<Integer>> propToObjectType = (Map)classToPropWithObjTypes.get(encodedClassIRI);
                    this.buildAndPruneShapes(confidence, support, b, encodedClassIRI, propToObjectType);
                });
                m = b.build();
                conn.add(m, new Resource[0]);
            });
        } else {
            classesList = null;
            ModelBuilder b = new ModelBuilder();
            Iterator var7 = classToPropWithObjTypes.entrySet().iterator();

            while(var7.hasNext()) {
                Map.Entry<Integer, Map<Integer, Set<Integer>>> entry = (Map.Entry)var7.next();
                Integer encodedClassIRI = (Integer)entry.getKey();
                Map<Integer, Set<Integer>> propToObjectType = (Map)entry.getValue();
                this.buildAndPruneShapes(confidence, support, b, encodedClassIRI, propToObjectType);
            }

            Model m = b.build();
            conn.add(m, new Resource[0]);
        }

    }

    private void buildAndPruneShapes(Double confidence, Integer support, ModelBuilder b, Integer encodedClassIRI, Map<Integer, Set<Integer>> propToObjectType) {
        if (Utils.isValidIRI(this.encoder.decode(encodedClassIRI))) {
            IRI subj = this.factory.createIRI(this.encoder.decode(encodedClassIRI));
            int classId = this.encoder.encode(subj.stringValue());
            int classInstances = (Integer)this.classInstanceCount.get(classId);
            if (support == 1) {
                if (classInstances >= support) {
                    this.prepareNodeAndPropertyShapes(confidence, support, b, encodedClassIRI, propToObjectType, subj);
                }
            } else if (classInstances > support) {
                this.prepareNodeAndPropertyShapes(confidence, support, b, encodedClassIRI, propToObjectType, subj);
            }
        } else {
            //Bug: Performance issues for Bear-B dataset. Classes are also not correctly resolved.
            //<http://dbpedia.org/resource/2015_US_Open_(tennis)> <http://dbpedia.org/ontology/budget> "4.22534E7"^^<http://dbpedia.org/datatype/usDollar> .
            //<http://dbpedia.org/datatype/usDollar> -> <%3http://dbpedia.org/datatype/usDollar%3>
            //Encoder var10001 = this.encoder;
            //System.out.println("constructShapesWithPruning:: INVALID SUBJECT IRI: " + var10001.decode(encodedClassIRI));
        }

    }

    private void prepareNodeAndPropertyShapes(Double confidence, Integer support, ModelBuilder b, Integer encodedClassIRI, Map<Integer, Set<Integer>> propToObjectType, IRI subj) {
        String var10000 = Constants.SHAPES_NAMESPACE;
        String nodeShape = var10000 + subj.getLocalName() + "Shape";
        b.subject(nodeShape).add(RDF.TYPE, SHACL.NODE_SHAPE).add(SHACL.TARGET_CLASS, subj);
        if (propToObjectType != null) {
            Map<Integer, Set<Integer>> propToObjectTypesLocalPositive = this.performPropShapePruningPositive(encodedClassIRI, propToObjectType, confidence, support);
            if (Main.qse_validation_with_shNot != null) {
                if (Boolean.parseBoolean(Main.qse_validation_with_shNot)) {
                    Map<Integer, Set<Integer>> propToObjectTypesLocalNegative = this.performPropShapePruningNegative(encodedClassIRI, propToObjectType, confidence, support);
                    this.constructPropertyShapesWithShNot(b, subj, encodedClassIRI, nodeShape, propToObjectTypesLocalPositive, propToObjectTypesLocalNegative);
                } else {
                    this.constructPropertyShapes(b, subj, encodedClassIRI, nodeShape, propToObjectTypesLocalPositive);
                }
            } else {
                this.constructPropertyShapes(b, subj, encodedClassIRI, nodeShape, propToObjectTypesLocalPositive);
            }
        }

    }

    private Map<Integer, Set<Integer>> performPropShapePruningPositive(Integer classEncodedLabel, Map<Integer, Set<Integer>> propToObjectType, Double confidence, Integer support) {
        Map<Integer, Set<Integer>> propToObjectTypesLocal = new HashMap();
        Iterator var6 = propToObjectType.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<Integer, Set<Integer>> entry = (Map.Entry)var6.next();
            Integer prop = (Integer)entry.getKey();
            Set<Integer> propObjectTypes = (Set)entry.getValue();
            HashSet<Integer> objTypesSet = new HashSet();
            IRI property = this.factory.createIRI(this.encoder.decode(prop));
            boolean isInstantTypeProperty = property.toString().equals(this.remAngBrackets(this.typePredicate));
            if (isInstantTypeProperty) {
                propToObjectTypesLocal.put(prop, objTypesSet);
            }

            double relativeSupport = 0.0;
            if (this.isSamplingOn) {
                relativeSupport = (double)support * Math.min((double)(Integer)this.sampledPropCount.get(prop) / (double)(Integer)this.propCount.get(prop), (double)((List)this.sampledEntitiesPerClass.get(classEncodedLabel)).size() / (double)(Integer)this.classInstanceCount.get(classEncodedLabel));
                if (this.supportToRelativeSupport.get(support) != null) {
                    ((List)this.supportToRelativeSupport.get(support)).add(relativeSupport);
                } else {
                    List<Double> list = new ArrayList();
                    list.add(relativeSupport);
                    this.supportToRelativeSupport.put(support, list);
                }
            }

            this.positivePruning(classEncodedLabel, confidence, support, prop, propObjectTypes, objTypesSet, relativeSupport);
            if (objTypesSet.size() != 0) {
                propToObjectTypesLocal.put(prop, objTypesSet);
            }
        }

        return propToObjectTypesLocal;
    }

    private Map<Integer, Set<Integer>> performPropShapePruningNegative(Integer classEncodedLabel, Map<Integer, Set<Integer>> propToObjectType, Double confidence, Integer support) {
        Map<Integer, Set<Integer>> propToObjectTypesLocal = new HashMap();
        Iterator var6 = propToObjectType.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<Integer, Set<Integer>> entry = (Map.Entry)var6.next();
            Integer prop = (Integer)entry.getKey();
            Set<Integer> propObjectTypes = (Set)entry.getValue();
            HashSet<Integer> objTypesSet = new HashSet();
            IRI property = this.factory.createIRI(this.encoder.decode(prop));
            boolean isInstantTypeProperty = property.toString().equals(this.remAngBrackets(this.typePredicate));
            if (isInstantTypeProperty) {
                propToObjectTypesLocal.put(prop, objTypesSet);
            }

            double relativeSupport = 0.0;
            if (this.isSamplingOn) {
                relativeSupport = (double)support * Math.min((double)(Integer)this.sampledPropCount.get(prop) / (double)(Integer)this.propCount.get(prop), (double)((List)this.sampledEntitiesPerClass.get(classEncodedLabel)).size() / (double)(Integer)this.classInstanceCount.get(classEncodedLabel));
                if (this.supportToRelativeSupport.get(support) != null) {
                    ((List)this.supportToRelativeSupport.get(support)).add(relativeSupport);
                } else {
                    List<Double> list = new ArrayList();
                    list.add(relativeSupport);
                    this.supportToRelativeSupport.put(support, list);
                }
            }

            this.negativePruning(classEncodedLabel, confidence, support, prop, propObjectTypes, objTypesSet, relativeSupport);
            if (objTypesSet.size() != 0) {
                propToObjectTypesLocal.put(prop, objTypesSet);
            }
        }

        return propToObjectTypesLocal;
    }

    private void positivePruning(Integer classEncodedLabel, Double confidence, Integer support, Integer prop, Set<Integer> propObjectTypes, HashSet<Integer> objTypesSet, double relativeSupport) {
        Iterator var9 = propObjectTypes.iterator();

        while(true) {
            while(true) {
                Integer encodedObjectType;
                Tuple3 tuple3;
                do {
                    if (!var9.hasNext()) {
                        return;
                    }

                    encodedObjectType = (Integer)var9.next();
                    tuple3 = new Tuple3(classEncodedLabel, prop, encodedObjectType);
                } while(!this.shapeTripletSupport.containsKey(tuple3));

                SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                if (support == 1 && sc.getConfidence() > confidence && sc.getSupport() >= support) {
                    objTypesSet.add(encodedObjectType);
                }

                if (this.isSamplingOn && support != 1) {
                    if (sc.getConfidence() > confidence && (double)sc.getSupport() > relativeSupport) {
                        objTypesSet.add(encodedObjectType);
                    }
                } else if (sc.getConfidence() > confidence && sc.getSupport() > support) {
                    objTypesSet.add(encodedObjectType);
                }
            }
        }
    }

    private void negativePruning(Integer classEncodedLabel, Double confidence, Integer support, Integer prop, Set<Integer> propObjectTypes, HashSet<Integer> objTypesSet, double relativeSupport) {
        Iterator var9 = propObjectTypes.iterator();

        while(true) {
            while(true) {
                Integer encodedObjectType;
                Tuple3 tuple3;
                do {
                    if (!var9.hasNext()) {
                        return;
                    }

                    encodedObjectType = (Integer)var9.next();
                    tuple3 = new Tuple3(classEncodedLabel, prop, encodedObjectType);
                } while(!this.shapeTripletSupport.containsKey(tuple3));

                SupportConfidence sc = (SupportConfidence)this.shapeTripletSupport.get(tuple3);
                if (support == 1 && sc.getConfidence() <= confidence && sc.getSupport() < support) {
                    objTypesSet.add(encodedObjectType);
                }

                if (this.isSamplingOn && support != 1) {
                    if (sc.getConfidence() <= confidence && (double)sc.getSupport() < relativeSupport) {
                        objTypesSet.add(encodedObjectType);
                    }
                } else if (sc.getConfidence() <= confidence && sc.getSupport() < support) {
                    objTypesSet.add(encodedObjectType);
                }
            }
        }
    }

    private void constructPropertyShapes(ModelBuilder b, IRI subj, Integer subjEncoded, String nodeShape, Map<Integer, Set<Integer>> propToObjectTypesLocal) {
        Map<String, Integer> propDuplicateDetector = new HashMap();
        List<PS> propertyShapes = new ArrayList();
        propToObjectTypesLocal.forEach((prop, propObjectTypes) -> {
            ModelBuilder localBuilder = new ModelBuilder();
            IRI property = this.factory.createIRI(this.encoder.decode(prop));
            String localName = property.getLocalName();
            PS ps = new PS();
            this.currPropertyShape = ps;
            boolean isInstanceTypeProperty = property.toString().equals(this.remAngBrackets(this.typePredicate));
            if (isInstanceTypeProperty) {
                localName = "instanceType";
            }

            if (propDuplicateDetector.containsKey(localName)) {
                int freq = (Integer)propDuplicateDetector.get(localName);
                propDuplicateDetector.put(localName, freq + 1);
                localName = localName + "_" + freq;
            }

            propDuplicateDetector.putIfAbsent(localName, 1);
            IRI propShape = this.factory.createIRI(Constants.SHAPES_NAMESPACE + localName + subj.getLocalName() + "ShapeProperty");
            b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            ps.setIri(propShape);
            ps.setPath(property.toString());
            if (isInstanceTypeProperty) {
                Resource head = Values.bnode();
                List<Resource> membersx = Arrays.asList(subj);
                Model tempModel = (Model)RDFCollections.asRDF(membersx, head, new LinkedHashModel(), new Resource[0]);
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                });
                tempModel.add(propShape, SHACL.IN, head, new Resource[0]);
                b.build().addAll(tempModel);
                b.build().addAll(localBuilder.build());
            }

            int numberOfObjectTypes = propObjectTypes.size();
            if (numberOfObjectTypes == 1 && !isInstanceTypeProperty) {
                ps.setHasOrList(false);
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    String objectType = this.encoder.decode(encodedObjectTypex);
                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                b.subject(propShape).add(SHACL.CLASS, objectTypeIri);
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                                ps.setNodeKind(SHACL.IRI.getLocalName());
                                ps.setDataTypeOrClass(objectTypeIri.toString());
                            } else {
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                                ps.setNodeKind(SHACL.IRI.getLocalName());
                                if (objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                    b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                                }
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.DATATYPE, objectTypeIri);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                            ps.setNodeKind(SHACL.LITERAL.getLocalName());
                            ps.setDataTypeOrClass(objectTypeIri.toString());
                        }
                    } else {
                        b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                        ps.setDataTypeOrClass(XSD.STRING.getLocalName());
                    }

                });
                b.build().addAll(localBuilder.build());
            }

            if (numberOfObjectTypes > 1) {
                ps.setHasOrList(true);
                List<Resource> members = new ArrayList();
                Resource headMember = Values.bnode();
                List<ShaclOrListItem> shaclOrListItems = new ArrayList();
                Iterator var19 = propObjectTypes.iterator();

                while(var19.hasNext()) {
                    Integer encodedObjectType = (Integer)var19.next();
                    ShaclOrListItem shaclOrListItem = new ShaclOrListItem();
                    this.currShaclOrListItem = shaclOrListItem;
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectType);
                    String objectType = this.encoder.decode(encodedObjectType);
                    Resource currentMember = Values.bnode();
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                localBuilder.subject(currentMember).add(SHACL.CLASS, objectTypeIri);
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                shaclOrListItem.setDataTypeOrClass(objectTypeIri.toString());
                                shaclOrListItem.setNodeKind(SHACL.IRI.getLocalName());
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            } else {
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                shaclOrListItem.setNodeKind(SHACL.IRI.getLocalName());
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            localBuilder.subject(currentMember).add(SHACL.DATATYPE, objectTypeIri);
                            localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            shaclOrListItem.setDataTypeOrClass(objectTypeIri.toString());
                            shaclOrListItem.setNodeKind(SHACL.LITERAL.getLocalName());
                            this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                        }
                    } else {
                        localBuilder.subject(currentMember).add(SHACL.DATATYPE, XSD.STRING);
                        shaclOrListItem.setDataTypeOrClass(XSD.STRING.getLocalName());
                    }

                    members.add(currentMember);
                    shaclOrListItems.add(shaclOrListItem);
                }

                Model localModel = (Model)RDFCollections.asRDF(members, headMember, new LinkedHashModel(), new Resource[0]);
                localModel.add(propShape, SHACL.OR, headMember, new Resource[0]);
                localModel.addAll(localBuilder.build());
                b.build().addAll(localModel);
                ps.setShaclOrListItems(shaclOrListItems);
            }

            propertyShapes.add(ps);
        });
        this.currNodeShape.setPropertyShapes(propertyShapes);
    }

    private void constructPropertyShapesWithShNot(ModelBuilder b, IRI subj, Integer subjEncoded, String nodeShape, Map<Integer, Set<Integer>> PropToObjectTypesPositive, Map<Integer, Set<Integer>> propToObjectTypesNegative) {
        Map<String, Integer> propDuplicateDetector = new HashMap();
        PropToObjectTypesPositive.forEach((prop, propObjectTypes) -> {
            ModelBuilder localBuilder = new ModelBuilder();
            IRI property = this.factory.createIRI(this.encoder.decode(prop));
            String localName = property.getLocalName();
            boolean isInstanceTypeProperty = property.toString().equals(this.remAngBrackets(this.typePredicate));
            if (isInstanceTypeProperty) {
                localName = "instanceType";
            }

            if (propDuplicateDetector.containsKey(localName)) {
                int freq = (Integer)propDuplicateDetector.get(localName);
                propDuplicateDetector.put(localName, freq + 1);
                localName = localName + "_" + freq;
            }

            propDuplicateDetector.putIfAbsent(localName, 1);
            IRI propShape = this.factory.createIRI(Constants.SHAPES_NAMESPACE + localName + subj.getLocalName() + "ShapeProperty");
            b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            if (isInstanceTypeProperty) {
                Resource head = Values.bnode();
                List<Resource> membersx = Arrays.asList(subj);
                Model tempModel = (Model)RDFCollections.asRDF(membersx, head, new LinkedHashModel(), new Resource[0]);
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                });
                tempModel.add(propShape, SHACL.IN, head, new Resource[0]);
                b.build().addAll(tempModel);
                b.build().addAll(localBuilder.build());
            }

            int numberOfObjectTypes = propObjectTypes.size();
            if (numberOfObjectTypes == 1 && !isInstanceTypeProperty) {
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    String objectType = this.encoder.decode(encodedObjectTypex);
                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                b.subject(propShape).add(SHACL.CLASS, objectTypeIri);
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                            } else {
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                                if (objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                    b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                                }
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.DATATYPE, objectTypeIri);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                        }
                    } else {
                        b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                    }

                });
                b.build().addAll(localBuilder.build());
            }

            if (numberOfObjectTypes > 1) {
                List<Resource> members = new ArrayList();
                Resource headMember = Values.bnode();

                BNode currentMember;
                for(Iterator var16 = propObjectTypes.iterator(); var16.hasNext(); members.add(currentMember)) {
                    Integer encodedObjectType = (Integer)var16.next();
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectType);
                    String objectType = this.encoder.decode(encodedObjectType);
                    currentMember = Values.bnode();
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                localBuilder.subject(currentMember).add(SHACL.CLASS, objectTypeIri);
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            } else {
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            localBuilder.subject(currentMember).add(SHACL.DATATYPE, objectTypeIri);
                            localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                        }
                    } else {
                        localBuilder.subject(currentMember).add(SHACL.DATATYPE, XSD.STRING);
                    }
                }

                Model localModel = (Model)RDFCollections.asRDF(members, headMember, new LinkedHashModel(), new Resource[0]);
                localModel.add(propShape, SHACL.OR, headMember, new Resource[0]);
                localModel.addAll(localBuilder.build());
                b.build().addAll(localModel);
            }

        });
        propToObjectTypesNegative.forEach((prop, propObjectTypes) -> {
            ModelBuilder localBuilder = new ModelBuilder();
            IRI property = this.factory.createIRI(this.encoder.decode(prop));
            String localName = property.getLocalName();
            boolean isInstanceTypeProperty = property.toString().equals(this.remAngBrackets(this.typePredicate));
            if (isInstanceTypeProperty) {
                localName = "instanceType";
            }

            if (propDuplicateDetector.containsKey(localName)) {
                int freq = (Integer)propDuplicateDetector.get(localName);
                propDuplicateDetector.put(localName, freq + 1);
                localName = localName + "_" + freq;
            }

            propDuplicateDetector.putIfAbsent(localName, 1);
            IRI shNotNodeShapeIriForPropShape = this.factory.createIRI(Constants.SHAPES_NAMESPACE + localName + subj.getLocalName() + "_PS_NotShape");
            b.subject(nodeShape).add(SHACL.NOT, shNotNodeShapeIriForPropShape);
            b.subject(shNotNodeShapeIriForPropShape).add(RDF.TYPE, SHACL.NODE_SHAPE);
            IRI propShape = this.factory.createIRI(Constants.SHAPES_NAMESPACE + localName + subj.getLocalName() + "ShapeProperty");
            b.subject(shNotNodeShapeIriForPropShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            if (isInstanceTypeProperty) {
                Resource head = Values.bnode();
                List<Resource> membersx = Arrays.asList(subj);
                Model tempModel = (Model)RDFCollections.asRDF(membersx, head, new LinkedHashModel(), new Resource[0]);
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                });
                tempModel.add(propShape, SHACL.IN, head, new Resource[0]);
                b.build().addAll(tempModel);
                b.build().addAll(localBuilder.build());
            }

            int numberOfObjectTypes = propObjectTypes.size();
            if (numberOfObjectTypes == 1 && !isInstanceTypeProperty) {
                propObjectTypes.forEach((encodedObjectTypex) -> {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectTypex);
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    String objectType = this.encoder.decode(encodedObjectTypex);
                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                b.subject(propShape).add(SHACL.CLASS, objectTypeIri);
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                            } else {
                                b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                                if (objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                    b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                                }
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.DATATYPE, objectTypeIri);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            this.annotateWithSupportAndConfidence(propShape, localBuilder, tuple3);
                        }
                    } else {
                        b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                    }

                });
                b.build().addAll(localBuilder.build());
            }

            if (numberOfObjectTypes > 1) {
                List<Resource> members = new ArrayList();
                Resource headMember = Values.bnode();

                BNode currentMember;
                for(Iterator var17 = propObjectTypes.iterator(); var17.hasNext(); members.add(currentMember)) {
                    Integer encodedObjectType = (Integer)var17.next();
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3(this.encoder.encode(subj.stringValue()), prop, encodedObjectType);
                    String objectType = this.encoder.decode(encodedObjectType);
                    currentMember = Values.bnode();
                    if (this.shapeTripletSupport.containsKey(tuple3)) {
                        if (((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport().equals(this.classInstanceCount.get(this.encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }

                        if (Main.extractMaxCardConstraints && this.propWithClassesHavingMaxCountOne.containsKey(prop) && ((Set)this.propWithClassesHavingMaxCountOne.get(prop)).contains(subjEncoded)) {
                            b.subject(propShape).add(SHACL.MAX_COUNT, this.factory.createLiteral(XMLDatatypeUtil.parseInteger("1")));
                        }
                    }

                    if (objectType != null) {
                        IRI objectTypeIri;
                        if (!objectType.contains("http://www.w3.org/2001/XMLSchema#") && !objectType.contains(RDF.LANGSTRING.toString())) {
                            if (Utils.isValidIRI(objectType) && !objectType.equals(Constants.OBJECT_UNDEFINED_TYPE)) {
                                objectTypeIri = this.factory.createIRI(objectType);
                                localBuilder.subject(currentMember).add(SHACL.CLASS, objectTypeIri);
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            } else {
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                            }
                        } else {
                            if (objectType.contains("<")) {
                                objectType = objectType.replace("<", "").replace(">", "");
                            }

                            objectTypeIri = this.factory.createIRI(objectType);
                            localBuilder.subject(currentMember).add(SHACL.DATATYPE, objectTypeIri);
                            localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            this.annotateWithSupportAndConfidence((Resource)currentMember, localBuilder, tuple3);
                        }
                    } else {
                        localBuilder.subject(currentMember).add(SHACL.DATATYPE, XSD.STRING);
                    }
                }

                Model localModel = (Model)RDFCollections.asRDF(members, headMember, new LinkedHashModel(), new Resource[0]);
                localModel.add(propShape, SHACL.OR, headMember, new Resource[0]);
                localModel.addAll(localBuilder.build());
                b.build().addAll(localModel);
            }

        });
    }

    private void annotateWithSupportAndConfidence(Resource currentMember, ModelBuilder localBuilder, Tuple3<Integer, Integer, Integer> tuple3) {
        if (this.shapeTripletSupport.containsKey(tuple3)) {
            Literal entities = Values.literal(((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport());
            localBuilder.subject(currentMember).add(Constants.SUPPORT, entities);
            Literal confidence = Values.literal(((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getConfidence());
            localBuilder.subject(currentMember).add(Constants.CONFIDENCE, confidence);
            this.currShaclOrListItem.setConfidence(confidence.doubleValue());
            this.currShaclOrListItem.setSupport(entities.intValue());
        }

    }

    private void annotateWithSupportAndConfidence(IRI propShape, ModelBuilder localBuilder, Tuple3<Integer, Integer, Integer> tuple3) {
        if (this.shapeTripletSupport.containsKey(tuple3)) {
            Literal entities = Values.literal(((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getSupport());
            localBuilder.subject(propShape).add(Constants.SUPPORT, entities);
            Literal confidence = Values.literal(((SupportConfidence)this.shapeTripletSupport.get(tuple3)).getConfidence());
            localBuilder.subject(propShape).add(Constants.CONFIDENCE, confidence);
            this.currPropertyShape.setConfidence(confidence.doubleValue());
            this.currPropertyShape.setSupport(entities.intValue());
        }

    }

    public HashMap<String, String> computeShapeStatistics(RepositoryConnection conn) {
        HashMap<String, String> shapesStats = new HashMap();

        int i;
        String type;
        TupleQuery query;
        Value queryOutput;
        Literal literalCount;
        for(i = 1; i <= 5; ++i) {
            type = "count";
            query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
            queryOutput = this.executeQuery(query, type);
            if (queryOutput.isLiteral()) {
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
            } else {
                shapesStats.put((String)ExperimentsUtil.getAverageHeader().get(i), "-999");
            }

            type = "max";
            query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
            queryOutput = this.executeQuery(query, type);
            if (queryOutput != null && queryOutput.isLiteral()) {
                literalCount = (Literal)queryOutput;
                shapesStats.put((String)ExperimentsUtil.getMaxHeader().get(i), literalCount.stringValue());
            } else {
                shapesStats.put((String)ExperimentsUtil.getMaxHeader().get(i), "-999");
            }

            type = "min";
            query = conn.prepareTupleQuery(FilesUtil.readShaclStatsQuery("query" + i, type));
            queryOutput = this.executeQuery(query, type);
            if (queryOutput != null && queryOutput.isLiteral()) {
                literalCount = (Literal)queryOutput;
                shapesStats.put((String)ExperimentsUtil.getMinHeader().get(i), literalCount.stringValue());
            } else {
                shapesStats.put((String)ExperimentsUtil.getMinHeader().get(i), "-999");
            }
        }

        return shapesStats;
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

    public String remAngBrackets(String typePredicate) {
        return typePredicate.replace("<", "").replace(">", "");
    }

    private static void performDirCheck(File dbDir) {
        if (dbDir.exists() && dbDir.delete()) {
            System.out.println("Deleted already existing directory. This will avoid duplication.");
        }

        if (!dbDir.exists()) {
            if (dbDir.mkdir()) {
                System.out.println(dbDir.getAbsoluteFile() + " created successfully.");
            } else {
                System.out.println("WARNING::directory creation failed");
            }
        }

    }

    public String writeModelToFile(String fileIdentifier, RepositoryConnection conn) {
        StopWatch watch = new StopWatch();
        watch.start();
        String path = Main.outputFilePath;
        String outputPath = path + Main.datasetName + "_" + fileIdentifier + ".ttl";
        System.out.println("::: ShapesExtractor ~ WRITING MODEL TO FILE: " + outputPath);
        GraphQuery query = conn.prepareGraphQuery("CONSTRUCT WHERE { ?s ?p ?o .}");
        Model model = QueryResults.asModel(query.evaluate());

        try {
            FileWriter fileWriter = new FileWriter(outputPath, false);
            Rio.write(model, fileWriter, RDFFormat.TURTLE);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        watch.stop();
        PrintStream var10000 = System.out;
        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
        var10000.println("writeModelToFile  - " + var10001 + " - " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
        return outputPath;
    }

    public void prettyFormatTurtle(String inputFilePath) {
        StopWatch watch = new StopWatch();
        watch.start();
        Path path = Paths.get(inputFilePath);
        String fileName = FilenameUtils.removeExtension(path.getFileName().toString()) + "_SHACL.ttl";
        String outputPath = Main.outputFilePath + fileName;
        this.outputFileAddress = outputPath;
        System.out.println("::: ShapesExtractor ~ PRETTY FORMATTING TURTLE FILE: " + outputPath);

        try {
            (new TurtlePrettyFormatter(inputFilePath)).format(outputPath);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        watch.stop();
        PrintStream var10000 = System.out;
        long var10001 = TimeUnit.MILLISECONDS.toSeconds(watch.getTime());
        var10000.println("prettyFormatTurtle  - " + var10001 + " - " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }

    public void setPropWithClassesHavingMaxCountOne(Map<Integer, Set<Integer>> propWithClassesHavingMaxCountOne) {
        this.propWithClassesHavingMaxCountOne = propWithClassesHavingMaxCountOne;
    }

    public void setSampledEntitiesPerClass(Map<Integer, List<Integer>> sampledEntitiesPerClass) {
        this.sampledEntitiesPerClass = sampledEntitiesPerClass;
    }

    public Boolean isSamplingOn() {
        return this.isSamplingOn;
    }

    public void setSamplingOn(Boolean samplingOn) {
        this.isSamplingOn = samplingOn;
    }

    public void setPropCount(Map<Integer, Integer> propCount) {
        this.propCount = propCount;
    }

    public void setSampledPropCount(Map<Integer, Integer> sampledPropCount) {
        this.sampledPropCount = this.propCount;
    }

    public String getOutputFileAddress() {
        return this.outputFileAddress;
    }
}