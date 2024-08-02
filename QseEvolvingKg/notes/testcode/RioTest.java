package com.example.application;

import jakarta.json.Json;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

//import org.eclipse.rdf4j.rio.Rio;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RioTest {
    public static void main(String[] args) throws Exception {
        test();
    }

        public static void test() throws IOException {
        //C:\Users\evapu\Documents\GitHub\qse\Output\TEMP\lubm-mini_QSE_0.1_100_SHACL.ttl
        File rdfFile = new File("C:\\Users\\evapu\\Documents\\GitHub\\qse\\Output\\TEMP\\lubm-mini_QSE_0.1_100_SHACL.ttl"); // Replace with your file path
        try (InputStream inputStream = new FileInputStream(rdfFile)) {
            RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE); // Replace with the appropriate format if needed

            Model model = new LinkedHashModel();
            rdfParser.setRDFHandler(new StatementCollector(model));

            // Parse the RDF file
            rdfParser.parse(inputStream, "");

            // Get the parsed statements as a Model

            // Print the parsed statements (just an example)
            for (Statement st : model) {
                System.out.println(st);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        InputStream inputStream = documentUrl.openStream();
//        //RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
//        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    }
}
