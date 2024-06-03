package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.ShapesExtractor;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
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
import java.util.stream.Collectors;

public class GraphDbUtils {
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

    public void checkShapesInNewGraph(String url, String repositoryName, List<NodeShape> nodeShapes) {
        RepositoryManager repositoryManager = new RemoteRepositoryManager(url);
        try {
            Repository repo = repositoryManager.getRepository(repositoryName);
            repo.init();
            try (RepositoryConnection conn = repo.getConnection()) {
                checkNodeShapesInNewGraph(conn, nodeShapes);
                checkPropertyShapesInNewGraph(conn, nodeShapes);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                repo.shutDown();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            repositoryManager.shutDown();
        }
    }

    //update with only one target class
    public void checkNodeShapesInNewGraph(RepositoryConnection conn, List<NodeShape> nodeShapes) {
        //        var targetClasses = nodeShapes.stream().flatMap(ns -> ns.targetClasses.stream().map(Object::toString)).collect(Collectors.toList());
        var targetClasses = nodeShapes.stream().map(ns -> ns.targetClass.toString()).collect(Collectors.toList());
        var filterString = String.join("> <", targetClasses);

        String sparql = "SELECT DISTINCT ?class (COUNT(DISTINCT ?s) AS ?classCount) FROM <http://www.ontotext.com/explicit> where {\n" +
                "\t?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                "VALUES ?class { <"+filterString+"> } }\n" +
                "Group by ?class";
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

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

    public void checkPropertyShapesInNewGraph(RepositoryConnection conn, List<NodeShape> nodeShapes) {
        for(var nodeShape : nodeShapes) {
            if (nodeShape.support != 0) { //performance
//                var targetClasses = nodeShape.targetClasses.stream().map(Object::toString).toList();
                var targetClasses = nodeShapes.stream().map(ns -> ns.targetClass.toString()).collect(Collectors.toList());
                var filterString = String.join("> <", targetClasses);
                //todo better way for performance?
                for (var propertyShape : nodeShape.propertyShapes) {
                    if (propertyShape.nodeKind != null && propertyShape.nodeKind.toString().equals("http://www.w3.org/ns/shacl#Literal")) {
                        propertyShape.support = getSupportForLiteralPropertyShape(propertyShape.path, propertyShape.dataType, filterString, conn);
                    }
                    else if(propertyShape.nodeKind != null && propertyShape.nodeKind.toString().equals("http://www.w3.org/ns/shacl#IRI")) {
                        propertyShape.support = getSupportForIriPropertyShape(propertyShape.path, propertyShape.classIri, filterString, conn);
                    }
                    else if (propertyShape.nodeKind == null) {
                        //Ignore special case when nodeKind is null, but there are also no nested items (QSE error)
                        if(propertyShape.orItems != null)  {
                            for(var orItem : propertyShape.orItems) {
                                if (orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#Literal")) {
                                    orItem.support = getSupportForLiteralPropertyShape(propertyShape.path, orItem.dataType, filterString, conn);
                                }
                                else if(orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#IRI")) {
                                    orItem.support = getSupportForIriPropertyShape(propertyShape.path, orItem.classIri, filterString, conn);
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

    private static int getSupportForIriPropertyShape(IRI path, IRI classIri, String filterString, RepositoryConnection conn) {
        String sparql = "SELECT ( COUNT( DISTINCT ?s) AS ?count) " +
                "FROM <http://www.ontotext.com/explicit> WHERE { " +
                " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ." +
                " ?s <" + path + "> ?obj . " +
                " ?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+ classIri+">" +
                " VALUES ?class { <" + filterString + "> }}";
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                return Integer.parseInt(bindingSet.getValue("count").stringValue());
            }
            throw new Exception("No support returned");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private static int getSupportForLiteralPropertyShape(IRI path, IRI dataType, String filterString, RepositoryConnection conn) {
        String sparql = "SELECT ( COUNT( DISTINCT ?s) AS ?count) " +
                "FROM <http://www.ontotext.com/explicit> WHERE { " +
                " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class ." +
                " ?s <" + path + "> ?obj . " +
                " FILTER(dataType(?obj) = <" + dataType + "> )" +
                " VALUES ?class { <" + filterString + "> }}";
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                return Integer.parseInt(bindingSet.getValue("count").stringValue());
            }
            throw new Exception("No support returned");
        }
        catch (Exception ex) {
            ex.printStackTrace();
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
                e.printStackTrace();
            }
        }
        targetFolder.mkdir();

        try {
            FileUtils.copyDirectory(originalFolder, targetFolder);
        } catch (IOException e) {
            e.printStackTrace();
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
