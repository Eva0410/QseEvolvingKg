package qseevolvingkg.partialsparqlqueries;

import cs.Main;
import cs.qse.common.PostConstraintsAnnotator;
import cs.qse.common.ShapesExtractor;
import cs.utils.Constants;
import cs.utils.FilesUtil;
import org.apache.commons.io.FileUtils;
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
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GraphDbUtils {
    public List<NodeShape> getNodeShapesWithTargetClassFromFile(String localDbFilePath) {
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
        var sparql = "select distinct ?ps where { " +
                "?shape <http://www.w3.org/ns/shacl#property> ?ps " +
                " filter(?shape = <"+nodeShape.iri+">)}";

        //todo get more infos for property shape
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        List<PropertyShape> propertyShapes = new ArrayList<>();
        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                var shapeIri = (IRI) bindingSet.getValue("ps");
                PropertyShape propertyShape = new PropertyShape();
                propertyShape.iri = shapeIri;
            }
        }
        nodeShape.propertyShapes = propertyShapes;
    }

    public List<NodeShape> checkNodeShapesInNewGraph(String url, String repositoryName, List<NodeShape> nodeShapes) {
        RepositoryManager repositoryManager = new RemoteRepositoryManager(url);
        try {
            Repository repo = repositoryManager.getRepository(repositoryName);
            repo.init();

            var targetClasses = nodeShapes.stream().flatMap(ns -> ns.targetClasses.stream().map(Object::toString)).collect(Collectors.toList());
            var filterString = String.join("> <", targetClasses);

            try (RepositoryConnection conn = repo.getConnection()) {
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

                        var nodeShape = nodeShapes.stream().filter(ns -> ns.targetClasses.stream().anyMatch(s -> s.equals(shapeIri))).findFirst().get();
                        nodeShape.support += support;
                    }
                }
            } finally {
                repo.shutDown();
            }
        } finally {
            repositoryManager.shutDown();
        }
        return nodeShapes;
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
}
