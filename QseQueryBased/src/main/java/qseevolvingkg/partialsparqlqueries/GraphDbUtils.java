package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.ShapesExtractor;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GraphDbUtils {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public List<NodeShape> getNodeShapesWithTargetClassFromRepo(String localDbFilePath) {
        Repository db = new SailRepository(new NativeStore(new File(localDbFilePath)));
        var nodeShapes = new ArrayList<NodeShape>();
        try (RepositoryConnection conn = db.getConnection()) {
            conn.setNamespace("shape", Constants.SHAPES_NAMESPACE);
            conn.setNamespace("shape", Constants.SHACL_NAMESPACE);

            var sparql = "select distinct ?shape ?targetClass where " +
                    "{?shape <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/shacl#NodeShape>." +
                    "?shape <http://www.w3.org/ns/shacl#targetClass> ?targetClass. }";
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            try (TupleQueryResult result = query.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    var shapeIri = (IRI) bindingSet.getValue("shape");
                    var targetClass = (IRI)bindingSet.getValue("targetClass");
                    var optionalExistingNS = nodeShapes.stream().filter(n -> n.iri.equals(shapeIri)).findFirst();
                    if(optionalExistingNS.isPresent()) {
                        optionalExistingNS.get().addTargetClasses(targetClass);
                    }
                    else {
                        NodeShape nodeShape = new NodeShape();
                        nodeShape.iri = shapeIri;
                        nodeShape.addTargetClasses(targetClass);
                        getPropertyShapesForNodeShape(nodeShape, conn);
                        nodeShapes.add(nodeShape);
                    }
                }
            }
        } finally {
            db.shutDown();
        }
        return nodeShapes;
    }


    private void getPropertyShapesForNodeShape(NodeShape nodeShape, RepositoryConnection conn) {
        var sparql = "select distinct ?ps ?nodeKind ?dataType ?path ?class ?nodeKindNested ?classNested ?dataTypeNested where { " +
                "?shape <http://www.w3.org/ns/shacl#property> ?ps." +
                "?ps <http://www.w3.org/ns/shacl#path> ?path . " +
                " OPTIONAL { ?ps <http://www.w3.org/ns/shacl#NodeKind> ?nodeKind }" +
                " OPTIONAL { ?ps <http://www.w3.org/ns/shacl#datatype> ?dataType } " +
                " OPTIONAL { ?ps <http://www.w3.org/ns/shacl#class> ?class } " +
                " OPTIONAL { ?ps <http://www.w3.org/ns/shacl#or> ?orList ." +
                "  ?orList rdf:rest*/rdf:first ?el . " +
                "  ?el <http://www.w3.org/ns/shacl#NodeKind> ?nodeKindNested. " +
                "   OPTIONAL { ?el <http://www.w3.org/ns/shacl#datatype> ?dataTypeNested. } " +
                "   OPTIONAL {?el <http://www.w3.org/ns/shacl#class> ?classNested } } " +
                " filter(?shape = <"+nodeShape.iri+">)}";

        //todo get more infos for property shape with in lists
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        List<PropertyShape> propertyShapes = new ArrayList<>();
        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                var shapeIri = (IRI) bindingSet.getValue("ps");
                var nodeKindValue = (IRI) bindingSet.getValue("nodeKind");
                var dataTypeValue = bindingSet.getValue("dataType");
                var classIriValue = bindingSet.getValue("class");
                var nodeKindNestedValue = bindingSet.getValue("nodeKindNested");
                var classIriNestedValue = bindingSet.getValue("classNested");
                var dataTypeNestedValue = bindingSet.getValue("dataTypeNested");

                IRI dataType = null;
                if(dataTypeValue != null)
                    dataType = (IRI) dataTypeValue;
                IRI classIri = null;
                if(classIriValue != null)
                    classIri = (IRI) classIriValue;
                IRI nodeKind = null;
                if(nodeKindValue != null)
                    nodeKind = (IRI) nodeKindValue;
                IRI nodeKindNested = null;
                if(nodeKindNestedValue != null)
                    nodeKindNested = (IRI) nodeKindNestedValue;
                IRI classIriNested = null;
                if(classIriNestedValue != null)
                    classIriNested = (IRI) classIriNestedValue;
                IRI dataTypeNested = null;
                if(dataTypeNestedValue != null)
                    dataTypeNested = (IRI) dataTypeNestedValue;
                var path = (IRI) bindingSet.getValue("path");

                var optionalExistingPs = propertyShapes.stream().filter(n -> n.iri.equals(shapeIri)).findFirst();
                if(optionalExistingPs.isPresent()) {
                   optionalExistingPs.get().addOrListItem(nodeKindNested, classIriNested, dataTypeNested);
                }
                else {
                    PropertyShape propertyShape = new PropertyShape();
                    propertyShape.iri = shapeIri;
                    propertyShape.nodeKind = nodeKind;
                    if(dataType != null && classIri != null)
                        throw new RuntimeException("Datatype and class are not null");
                    if(dataType == null)
                        propertyShape.dataTypeOrClass = classIri;
                    else
                        propertyShape.dataTypeOrClass = dataType;
                    propertyShape.dataType = dataType;
                    propertyShape.classIri = classIri;
                    propertyShape.path = path;
                    if(nodeKindNested != null && classIriNested != null)
                        propertyShape.addOrListItem(nodeKindNested, classIriNested, dataTypeNested);
                    propertyShapes.add(propertyShape);
                }
            }
        }
        nodeShape.propertyShapes = propertyShapes;
    }

    public static void checkShapesInNewGraph(String url, String repositoryName, List<NodeShape> nodeShapes) {
        RepositoryManager repositoryManager = new RemoteRepositoryManager(url);
        try {
            Repository repo = repositoryManager.getRepository(repositoryName);
            repo.init();
            try (RepositoryConnection conn = repo.getConnection()) {
                checkNodeShapesInNewGraph(conn, nodeShapes);
                checkPropertyShapesInNewGraph(conn, nodeShapes);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Exception occurred", ex);
            } finally {
                repo.shutDown();
            }
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred", ex);
        } finally {
            repositoryManager.shutDown();
        }
    }

    //update with only one target class
    public static void checkNodeShapesInNewGraph(RepositoryConnection conn, List<NodeShape> nodeShapes) {
        //        var targetClasses = nodeShapes.stream().flatMap(ns -> ns.targetClasses.stream().map(Object::toString)).collect(Collectors.toList());
        var targetClasses = nodeShapes.stream().map(ns -> ns.targetClass.toString()).collect(Collectors.toList());
        var filterString = String.join("> <", targetClasses);

        String sparql = "SELECT DISTINCT ?class (COUNT(DISTINCT ?s) AS ?classCount) FROM <http://www.ontotext.com/explicit> where {\n" +
                "\t?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                "VALUES ?class { <"+filterString+"> } }\n" +
                "Group by ?class";
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

        //set all support values to 0
        //todo problems here?
        nodeShapes.forEach(n -> n.support = 0);

        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                var shapeIri = (IRI) bindingSet.getValue("class");
                var support = Integer.parseInt(bindingSet.getValue("classCount").stringValue());
                //                var nodeShape = nodeShapes.stream().filter(ns -> ns.targetClasses.stream().anyMatch(s -> s.equals(shapeIri))).findFirst().get();
                var nodeShape = nodeShapes.stream().filter(ns -> ns.targetClass.equals(shapeIri)).findFirst().get();
                nodeShape.support = support; //Was += before
            }
        }
    }

    public static void checkPropertyShapesInNewGraph(RepositoryConnection conn, List<NodeShape> nodeShapes) {
        for(var nodeShape : nodeShapes) {
            if (nodeShape.support != 0) { //performance
//                var targetClasses = nodeShape.targetClasses.stream().map(Object::toString).toList();
                var targetClass = nodeShape.targetClass.toString();
                //todo better way for performance?
                for (var propertyShape : nodeShape.propertyShapes) {
//                    if(propertyShape.iri.toString().contains("http://shaclshapes.org/lengthThingShapeProperty"))
//                        System.out.println(); //todo remove
                    //Todo merge methods?
                    if (propertyShape.nodeKind != null && propertyShape.nodeKind.toString().equals("http://www.w3.org/ns/shacl#Literal")) {
                        propertyShape.support = getSupportForLiteralPropertyShape(propertyShape.path, propertyShape.dataTypeOrClass, targetClass, conn);
                    }
                    else if(propertyShape.nodeKind != null && propertyShape.nodeKind.toString().equals("http://www.w3.org/ns/shacl#IRI")) {
                        propertyShape.support = getSupportForIriPropertyShape(propertyShape.path, propertyShape.dataTypeOrClass, targetClass, conn);
                    }
                    else if (propertyShape.nodeKind == null) {
                        //Ignore special case when nodeKind is null, but there are also no nested items (QSE error)
                        if(propertyShape.orItems != null)  {
                            for(var orItem : propertyShape.orItems) {
                                if (orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#Literal")) {
                                    orItem.support = getSupportForLiteralPropertyShape(propertyShape.path, orItem.dataTypeOrClass, targetClass, conn);
                                }
                                else if(orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#IRI")) {
                                    orItem.support = getSupportForIriPropertyShape(propertyShape.path, orItem.dataTypeOrClass, targetClass, conn);
                                }
                            }
                        }
                        else {
                            propertyShape.errorDuringGeneration = true;
                        }
                    }
                }
            }
        }
    }

    public static String deleteOrListAndConnectToParentNode(String shape, String parentIri, int newSupport) {
        var model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        //problem with "," in confidence, this is read as two statements
        shape = shape.replaceAll("(?<=\\d),(?=\\d)", ".");

        model.read(new java.io.StringReader(shape), null, "TURTLE");
        Resource propertyShape = ResourceFactory.createResource(parentIri);

        String queryString = String.format("SELECT ?orList ?p ?o WHERE { " +
                "    <%s> <http://www.w3.org/ns/shacl#or> ?orList ." +
                "   ?orList <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?f. " +
                "   ?f ?p ?o. }", parentIri);

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        var statements = new ArrayList<Statement>();
        Resource orListItem = null;

        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                var o = soln.get("o");
                var p = soln.get("p").as(Property.class);
                orListItem = soln.getResource("orList");
                Statement s = ResourceFactory.createStatement(propertyShape, p, o);
                statements.add(s);
            }
        } finally {
            qexec.close();
        }
        //connect all connected statements to parent node
        model.add(statements);

        //Delete or list and all recursive statements
        model.removeAll(null, null, orListItem);
        removeRecursively(model, orListItem);
        var iriConfidence = ResourceFactory.createProperty("http://shaclshapes.org/confidence");
        var iriSupport = ResourceFactory.createProperty("http://shaclshapes.org/support");
        Literal confidenceLiteral = model.createTypedLiteral("1E0", XSD.xdouble.getURI());
        Literal supportLiteral = model.createTypedLiteral(newSupport);

        //set confidence to 100 % and new support
        setSupportOrConfidence(model, propertyShape, iriConfidence, confidenceLiteral);
        setSupportOrConfidence(model, propertyShape, iriSupport, supportLiteral);


        TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
        OutputStream outputStream = new ByteArrayOutputStream();
        formatter.accept(model, outputStream);
        return outputStream.toString().replaceAll("\n+$", "");
    }

    private static void setSupportOrConfidence(Model model, Resource propertyShape, Property iri, Literal newLiteral) {
        StmtIterator iterConfidence = model.listStatements(propertyShape, iri, (RDFNode) null);
        List<Statement> statementsToRemove = new ArrayList<>();
        while (iterConfidence.hasNext()) {
            Statement stmt = iterConfidence.nextStatement();
            statementsToRemove.add(stmt);
        }
        iterConfidence.close();
        for (Statement stmt : statementsToRemove) {
            model.remove(stmt);
        }
        model.add(propertyShape, iri, newLiteral);
    }

    private static Model removeRecursively(Model model, Resource resourceToDelete) {
        var statementQueue = model.listStatements(resourceToDelete, null, (RDFNode) null).toList();
        while (!statementQueue.isEmpty()) {
            var nextStatement = statementQueue.get(0);
            if(nextStatement.getObject().isAnon()) {
                statementQueue.addAll(model.listStatements((Resource) nextStatement.getObject(), null, (RDFNode)null ).toList());
            }
            model.remove(nextStatement);
            statementQueue.remove(nextStatement);
        }
        return model;
    }

    private static int getSupportForIriPropertyShape(IRI path, IRI classIri, String targetClass, RepositoryConnection conn) {
        String sparql;
        //special case where object does not have a type
        if(classIri.toString().equals("http://shaclshapes.org/undefined")) {
            sparql = "PREFIX onto: <http://www.ontotext.com/>\n" +
                    "SELECT ( COUNT( DISTINCT ?s) AS ?count) FROM onto:explicit WHERE {\n" +
                    "    ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                    "    ?s <" + path + "> ?obj .\n" +
                    " VALUES ?class { <" + targetClass + "> } \n" +
                    "    FILTER NOT EXISTS {?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?objDataType}\n" +
                    "}";
        }
        else {
            //special case where object is actually literal but still has datatype e.g. 3,2^^kilometre
            sparql = "SELECT ( COUNT( DISTINCT ?s) AS ?count) \n" +
                    "FROM <http://www.ontotext.com/explicit> WHERE { \n" +
                    " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                    " ?s <" + path + "> ?obj . \n" +
                    " optional{?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?dataTypeRdfType}. \n" +
                    " BIND (datatype(?obj) AS ?dataTypeLiteral) \n" +
                    " VALUES ?class { <" + targetClass + "> }" +
                    " FILTER (?dataTypeRdfType = <"+classIri+"> || ?dataTypeLiteral = <"+classIri+">)}";
        }
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                return Integer.parseInt(bindingSet.getValue("count").stringValue());
            }
            throw new Exception("No support returned");
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred", ex);
            return 0;
        }
    }

    private static int getSupportForLiteralPropertyShape(IRI path, IRI dataType, String targetClass, RepositoryConnection conn) {
        String sparql = "SELECT ( COUNT( DISTINCT ?s) AS ?count) " +
                "FROM <http://www.ontotext.com/explicit> WHERE { " +
                " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ." +
                " ?s <" + path + "> ?obj . " +
                " FILTER(dataType(?obj) = <" + dataType + "> )" +
                " VALUES ?class { <" + targetClass + "> }}";
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                return Integer.parseInt(bindingSet.getValue("count").stringValue());
            }
            throw new Exception("No support returned");
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred", ex);
            return 0;
        }
    }

    public String cloneSailRepository(String originalFileRepo, String secondVersionName) {
        File originalFolder = new File(originalFileRepo);
        File parentFolder = originalFolder.getParentFile();
        File targetFolder = new File(parentFolder, secondVersionName);

        if (targetFolder.exists()) {
            try {
                FileUtils.deleteDirectory(targetFolder);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occurred", e);
            }
        }
        targetFolder.mkdir();

        try {
            FileUtils.copyDirectory(originalFolder, targetFolder);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occurred", e);
        }
        return targetFolder.getAbsolutePath();
    }

    //unused, deletion per query does not work
//    public void deleteFromRepoWhereSupportIsZero(String filePath, List<NodeShape> nodeShapes) {
//        Repository db = new SailRepository(new NativeStore(new File(filePath)));
//        try (RepositoryConnection conn = db.getConnection()) {
//            var targetClasses = nodeShapes.stream().filter(ns -> ns.support == 0).flatMap(ns -> ns.targetClasses.stream().map(Object::toString)).collect(Collectors.toList());
//            //no better way found to delete multiple triples
//            for (var targetClass : targetClasses) {
//                String sparql = "delete where {\n" +
//                        "\t ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/shacl#NodeShape> .\n " +
//                        " ?s <http://www.w3.org/ns/shacl#targetClass> <"+targetClass+"> . " +
//                        " ?s ?p ?o ." +
//                        " }";
//                conn.prepareUpdate(QueryLanguage.SPARQL, sparql).execute();
//            }
//        } finally {
//            db.shutDown();
//        }
//    }

    public void deleteFromRepoWhereSupportIsZero(String filePath, List<NodeShape> nodeShapes) {

        Repository db = new SailRepository(new NativeStore(new File(filePath)));
        try (RepositoryConnection conn = db.getConnection()) {
            var targetClasses = nodeShapes.stream().filter(ns -> ns.support == 0).flatMap(ns -> ns.targetClasses.stream().map(Object::toString)).collect(Collectors.toList());
            //no better way found to delete multiple triples
            for (var targetClass : targetClasses) {
                String sparql = "delete where {\n" +
                        "\t ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/shacl#NodeShape> .\n " +
                        " ?s <http://www.w3.org/ns/shacl#targetClass> <"+targetClass+"> . " +
                        " ?s ?p ?o ." +
                        " }";
                conn.prepareUpdate(QueryLanguage.SPARQL, sparql).execute();
            }
        } finally {
            db.shutDown();
        }
    }

    public void constructDefaultShapes(String path) {
        Repository db = new SailRepository(new NativeStore(new File(path)));
        ShapesExtractor shapesExtractor = new ShapesExtractor();
        try (RepositoryConnection conn = db.getConnection()) {
            conn.setNamespace("shape", Constants.SHAPES_NAMESPACE);
            conn.setNamespace("shape", Constants.SHACL_NAMESPACE);

            String outputFilePath = shapesExtractor.writeModelToFile("QSE_FULL", conn);
            shapesExtractor.prettyFormatTurtle(outputFilePath);
            FilesUtil.deleteFile(outputFilePath);
        } finally {
            db.shutDown();
        }
    }

    public String getStringAsShape(String shape, NodeShape nodeShape) {
        var model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(shape), null, "TURTLE");

        //would need prefix lines for this
//        org.apache.jena.rdf.model.Resource iriSupport = ResourceFactory.createResource("http://shaclshapes.org/support");
//        org.apache.jena.rdf.model.Resource iriConfidence = ResourceFactory.createResource("http://shaclshapes.org/confidence");
//
//        String queryString = String.format("CONSTRUCT {?s ?p ?o} WHERE { ?s ?p ?o. FILTER (?p != <%s> && ?p != <%s>)}", iriSupport, iriConfidence);

//        var query = QueryFactory.create(queryString);
//        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
//            org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
//            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
//            OutputStream outputStream = new ByteArrayOutputStream();
//            formatter.accept(jenaModel, outputStream);
//            String cleanedString = reorderShaclInItems(outputStream.toString());
//            String cleanedStringOrItems = reOrderOrItems(cleanedString);
//            return cleanedStringOrItems.replaceAll("\n+$", "");
//        }
        return "";
    }
}
