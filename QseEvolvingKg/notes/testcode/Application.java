package com.example.application;

//import com.vaadin.flow.component.page.AppShellConfigurator;
//import com.vaadin.flow.theme.Theme;
//import org.apache.jena.graph.Graph;
//import org.apache.jena.graph.Node;
//import org.apache.jena.graph.compose.Delta;
//import org.apache.jena.query.Dataset;
//import org.apache.jena.query.DatasetFactory;
//import org.apache.jena.rdf.model.*;
//import org.apache.jena.riot.RDFDataMgr;
//import org.apache.jena.riot.RDFFormat;
//import org.apache.jena.sparql.core.DatasetGraph;
//import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.system.Txn;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.util.FileManager;
//import org.seaborne.patch.RDFPatch;
//import org.seaborne.patch.RDFPatchOps;
//import org.seaborne.patch.text.RDFChangesWriterText;
import org.seaborne.patch.RDFPatch;
import org.seaborne.patch.RDFPatchOps;
import org.seaborne.patch.changes.RDFChangesCollector;
import org.seaborne.patch.text.RDFChangesWriterText;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "my-app")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) throws IOException {
        //SpringApplication.run(Application.class, args);
        //test3();
        //ttl_to_nt();
        //test6();
        DatasetGraph dsgBase = DatasetGraphFactory.createTxnMem();

        // -- Destination for changes.
        // Text form of output.
        OutputStream out = System.out;
        // Create an RDFChanges that writes to "out".
        try ( RDFChangesWriterText changeLog = RDFPatchOps.textWriter(out) ) {

            // ---- Collect up changes.
            //RDFPatchOps.collect();
            RDFChangesCollector rcc = new RDFChangesCollector();
            DatasetGraph dsg = RDFPatchOps.changes(dsgBase, rcc);
            Dataset ds = DatasetFactory.wrap(dsg);
            Txn.executeWrite(ds,
                    ()-> RDFDataMgr.read(dsg, "data.ttl")
            );
            // Again - different bnodes.
            // Note all changes are recorded - even if they have no effect
            // (e.g the prefix, the triple "ex:s ex:p ex:o").
            Txn.executeWrite(ds,
                    ()->RDFDataMgr.read(dsg, "data.ttl")
            );

            // Collected (in-memory) patch.
            RDFPatch patch = rcc.getRDFPatch();
            // Write it.
            patch.apply(changeLog);
        }
    }

//    static void test4() {
//        DatasetGraph dsgBase = DatasetGraphFactory.createTxnMem();
//
//        // -- Destination for changes.
//        // Text form of output.
//        OutputStream out = System.out;
//        // Create an RDFChanges that writes to "out".
//        try ( RDFChangesWriterText changeLog = RDFPatchOps.textWriter(out) ) {
//
//            // Combined DatasetGraph and changes.
//            DatasetGraph dsg = RDFPatchOps.changes(dsgBase, changeLog);
//
//            // Wrap in the Dataset API
//            Dataset ds = DatasetFactory.wrap(dsg);
//
//            // --------
//            // Do something. Read in data.ttl inside a transaction.
//            // (data.ttl is in src/main/resources/)
//            Txn.executeWrite(ds,
//                    () -> RDFDataMgr.read(dsg, "data.ttl")
//            );
//        }
//    }

//    static void ttl_to_nt() {
//        String inputTTLFilePath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film.ttl";
//        String outputNTFilePath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film.nt";
//
//        // Create an empty Jena model
//        Model model = ModelFactory.createDefaultModel();
//
//        // Read data from the input TTL file into the model
//        model.read(inputTTLFilePath, "TURTLE");
//
//        // Write the model data to an N-Triples file
//        try (OutputStream outputStream = new FileOutputStream(outputNTFilePath)) {
//            RDFDataMgr.write(outputStream, model, RDFFormat.NTRIPLES);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    //example to compare graphs
//    static void test() {
//        // Provide the path to your .nt file
//        String filePath = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film.nt";
//        String filePath1 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film1.nt";
//
////        Model model = ModelFactory.createDefaultModel() ;
////        model.read(filePath, "NTRIPLES") ;
//        Graph graph = org.apache.jena.graph.Factory.createDefaultGraph();
//        RDFDataMgr.read(graph, filePath1);
//
//
//
//        // Iterate through the triples in the graph
////        System.out.println("Triples in the model:");
////        ResIterator iter = model.listSubjects();
////        while (iter.hasNext()) {
////            Resource subject = iter.nextResource();
////            StmtIterator stmtIter = subject.listProperties();
////            while (stmtIter.hasNext()) {
////                Statement stmt = stmtIter.nextStatement();
////                System.out.println(stmt);
////            }
////        }
//
//        System.out.println("Triples in the graph:");
//        graph.find().forEachRemaining(triple -> {
//            Node subject = triple.getSubject();
//            Node predicate = triple.getPredicate();
//            Node object = triple.getObject();
//
//            System.out.println(subject + " " + predicate + " " + object);
//        });
//    }

//    static void test2() {
//        String filePath1 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film.nt";
//        String filePath2 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film1.nt";
//
//        Graph graph1 = org.apache.jena.graph.Factory.createDefaultGraph();
//        Graph graph2 = org.apache.jena.graph.Factory.createDefaultGraph();
//
//        // Read data from the files into the graphs
//        RDFDataMgr.read(graph1, filePath1);
//        RDFDataMgr.read(graph2, filePath2);
//
//        // Compare the triples in the two graphs
//        System.out.println("Triples in graph1 not present in graph2:");
//        findDifference(graph1, graph2);
//        System.out.println("\nTriples in graph2 not present in graph1:");
//        findDifference(graph2, graph1);
//    }

//    static void test3() {
//        String filePath1 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film_.nt";
//        String filePath2 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film1_.nt";
//
//        Graph graph1 = org.apache.jena.graph.Factory.createDefaultGraph();
//        Graph graph2 = org.apache.jena.graph.Factory.createDefaultGraph();
//
//        RDFDataMgr.read(graph1, filePath1);
//        RDFDataMgr.read(graph2, filePath2);
//
//        // Read data from the files into the graphs
//        Set<String> set1 = convertToSet(graph1);
//        Set<String> set2 = convertToSet(graph2);
//
//        // Find the differences using set operations
//        Set<String> diff1 = new HashSet<>(set1);
//        diff1.removeAll(set2); // Triples in graph1 not present in graph2
//
//        Set<String> diff2 = new HashSet<>(set2);
//        diff2.removeAll(set1);
//        System.out.println("Triples in graph1 not present in graph2:");
//
//        for (String s: diff1
//             ) {
//            System.out.println(s);
//        }
//        System.out.println("\nTriples in graph2 not present in graph1:");
//
//        for (String s: diff2
//        ) {
//            System.out.println(s);
//        }
//    }

//    static void test6() {
//        String filePath1 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film.nt";
//        String filePath2 = "C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\graphs\\film1.nt";
//
//        Graph graph1 = org.apache.jena.graph.Factory.createDefaultGraph();
//        Graph graph2 = org.apache.jena.graph.Factory.createDefaultGraph();
//
//        RDFDataMgr.read(graph1, filePath1);
//        RDFDataMgr.read(graph2, filePath2);
//
//        Model model = ModelFactory.createModelForGraph(graph1);
//        Model model2 = ModelFactory.createModelForGraph(graph2);
//
//        // Get differences between graphs
//        Model addedStatements = model2.difference(model);
//        Model removedStatements = model.difference(model2);
//        System.out.println("Added Statements:");
//        addedStatements.listStatements().forEachRemaining(statement -> {
//            System.out.println(statement);
//            // Perform operations with added statements if needed
//        });
//
//        System.out.println("\nRemoved Statements:");
//        removedStatements.listStatements().forEachRemaining(statement -> {
//            System.out.println(statement);
//            // Perform operations with removed statements if needed
//        });
//    }
//
//    public static void findDifference(Graph graph1, Graph graph2) {
//        graph1.find().forEachRemaining(triple -> {
//            if (!graph2.contains(triple)) {
//                Node subject = triple.getSubject();
//                Node predicate = triple.getPredicate();
//                Node object = triple.getObject();
//
//                System.out.println(subject + " " + predicate + " " + object);
//            }
//        });
//    }
//
//    private static Set<String> convertToSet(Graph graph) {
//        Set<String> tripleSet = new HashSet<>();
//        graph.find().forEachRemaining(triple -> {
//            tripleSet.add(triple.toString());
//        });
//        return tripleSet;
//    }

}
