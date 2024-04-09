import com.oracle.truffle.js.runtime.builtins.JSArray;
import com.vaadin.flow.component.UI;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import elemental.json.JsonArray;
import org.apache.jena.iri.IRI;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import qseevolvingkgwebapp.services.Utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//Just debugging and trying
public class Test {

    @org.junit.Test
    public void reorderOR() {
        String shape = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "\n" +
                "<http://shaclshapes.org/attendanceThingShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "<http://www.w3.org/ns/shacl#or> ( [\n" +
                "<http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "<http://www.w3.org/ns/shacl#datatype> xsd:integer ;\n" +
                "] [\n" +
                "<http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#Literal> ;\n" +
                "<http://www.w3.org/ns/shacl#datatype> rdf:langString ;\n" +
                "] ) ;\n" +
                "<http://www.w3.org/ns/shacl#path> <http://dbpedia.org/property/attendance> .";
        System.out.println(Utils.reOrderOrItems(shape));
    }

    @org.junit.Test
//    public void test() {
    //PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    //
    //SELECT ?s ?p ?o
    //    WHERE {
    //      <http://shaclshapes.org/rangeDatatypePropertyShapeProperty> (rdf:type|!rdf:type)* ?s .
    //      ?s ?p ?o
    //    FILTER (?s = <http://shaclshapes.org/rangeDatatypePropertyShapeProperty> || isBlank(?s))
    //    }
    //would also work like this, but somehow other blank nodes are also included
//        try(FileInputStream inputStream = new FileInputStream("C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\Output\\film-1-Original_QSE_FULL_SHACL.ttl")) {
//            var model = ModelFactory.createDefaultModel();
//            RDFDataMgr.read(model, inputStream, RDFLanguages.TTL);
//            Resource iri = ResourceFactory.createResource("http://shaclshapes.org/rangeDatatypePropertyShapeProperty");
//
//            String queryString = String.format("CONSTRUCT {<%s> ?p ?o} WHERE { <%s> ?p ?o}", iri,iri);
//
//            var query = QueryFactory.create(queryString);
//            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
//                org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
//                addBlankNodesToModel(jenaModel, model);
//                TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
//                OutputStream outputStream = new ByteArrayOutputStream();
//                formatter.accept(jenaModel, outputStream);
//                System.out.println(outputStream.toString().replaceAll("\n+$", ""));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void generateText_includeSpecialCharacters() {

        StringBuilder fileContent = new StringBuilder();
        StringBuilder prefixLines = new StringBuilder();

        // Open the text file for reading
        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\Output\\bearb-1-Original_QSE_FULL_SHACL.ttl"))) {
            String line;

            // Read the file line by line and append to StringBuilder
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
                if (line.contains("@prefix"))
                    prefixLines.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Resource iri = ResourceFactory.createResource("http://shaclshapes.org/topScorer(s)_Q476028ShapeProperty");
        String iriWithEscapedChars = iri.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.", iriWithEscapedChars);

        // Compile the regular expression pattern
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(fileContent.toString());

        matcher.find();
        String match = matcher.group();
//        System.out.println(match);

        var model = ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(prefixLines + match), null, "TURTLE"); // Assuming Turtle format, change as needed

        Resource iriSupport = ResourceFactory.createResource("http://shaclshapes.org/support");
        Resource iriConfidence = ResourceFactory.createResource("http://shaclshapes.org/confidence");


        String queryString = String.format("CONSTRUCT {?s ?p ?o} WHERE { ?s ?p ?o. FILTER (?p != <%s> && ?p != <%s>)}", iriSupport, iriConfidence);

        var query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream outputStream = new ByteArrayOutputStream();
            formatter.accept(jenaModel, outputStream);
            System.out.println(outputStream.toString().replaceAll("\n+$", ""));
        }
    }

    public void test() {
        StringBuilder fileContent = new StringBuilder();
        StringBuilder prefixLines = new StringBuilder();

        // Open the text file for reading
        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\Output\\film-1-Original_QSE_FULL_SHACL.ttl"))) {
            String line;

            // Read the file line by line and append to StringBuilder
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
                if(line.contains("@prefix"))
                    prefixLines.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String regexPattern = "\n<http://shaclshapes.org/rangeDatatypePropertyShapeProperty>.*? \\.";

        // Compile the regular expression pattern
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(fileContent.toString());

        matcher.find();
        String match = matcher.group();
//        System.out.println(match);

        var model = ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(prefixLines+match), null, "TURTLE"); // Assuming Turtle format, change as needed

        Resource iri = ResourceFactory.createResource("http://shaclshapes.org/rangeDatatypePropertyShapeProperty");
        Resource iriSupport = ResourceFactory.createResource("http://shaclshapes.org/support");
        Resource iriConfidence = ResourceFactory.createResource("http://shaclshapes.org/confidence");


        String queryString = String.format("CONSTRUCT {?s ?p ?o} WHERE { ?s ?p ?o. FILTER (?p != <%s> && ?p != <%s>)}", iriSupport, iriConfidence);

        var query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream outputStream = new ByteArrayOutputStream();
            formatter.accept(jenaModel, outputStream);
            System.out.println(outputStream.toString().replaceAll("\n+$", ""));
        }

//        TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
//        OutputStream outputStream = new ByteArrayOutputStream();
//        formatter.accept(model, outputStream);
//        System.out.println(outputStream.toString().replaceAll("\n+$", ""));
//        RDFDataMgr.read(model, new java.io.StringReader(txt), RDFLanguages.TTL);


//        try(FileInputStream inputStream = new FileInputStream("C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\Output\\film-1-Original_QSE_FULL_SHACL.ttl")) {
//
//            var model = ModelFactory.createDefaultModel();
//            RDFDataMgr.read(model, inputStream, RDFLanguages.TTL);
//            Resource iri = ResourceFactory.createResource("http://shaclshapes.org/rangeDatatypePropertyShapeProperty");
//
//            String queryString = String.format("CONSTRUCT {<%s> ?p ?o} WHERE { <%s> ?p ?o}", iri,iri);
//
//            var query = QueryFactory.create(queryString);
//            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
//                org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
//                addBlankNodesToModel(jenaModel, model);
//                TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
//                OutputStream outputStream = new ByteArrayOutputStream();
//                formatter.accept(jenaModel, outputStream);
//                System.out.println(outputStream.toString().replaceAll("\n+$", ""));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


//    private static Set<Statement> addBlankNodesToModel(org.apache.jena.rdf.model.Model filteredModel, Model model) {
//        var blankNodeQueue = filteredModel.stream().filter(statement -> statement.getObject() instanceof BNode).collect(Collectors.toList());
//        while(!blankNodeQueue.isEmpty()) {
//            var nextStatement = blankNodeQueue.get(0);
//            var modelToAdd = model.stream().filter(statement -> statement.getSubject().equals(nextStatement.getObject())).toList();
//            filteredModel.addAll(modelToAdd);
//            blankNodeQueue.remove(nextStatement);
//            var tmp = modelToAdd.stream().filter(statement -> statement.getObject() instanceof BNode).toList();
//            blankNodeQueue.addAll(tmp);
//        }
//        return filteredModel;
//    }

//    @org.junit.Test
//    public void testJSError() {
//        //string too long, not testable
////        var js = "    const originalText = '" + t1 + "';" +
////                "    const modifiedText = '" + t2 + "';" +
////                "    const diff = Diff.diffWords(originalText, modifiedText);" +
////                "    return diff;";
////        UI.getCurrent().getPage().executeJs(js).then(response -> {
////            if (response instanceof JsonArray) {
////                System.out.println("worked");
////            }
////        });
//    }
//
//    @org.junit.Test
//    public void testJS() {
//        ScriptEngineManager manager = new ScriptEngineManager();
//
//        // Get JavaScript engine
//        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
//
//        if (engine != null) {
//            // JavaScript code to execute
//            String jsCode = "import('https://cdnjs.cloudflare.com/ajax/libs/jsdiff/5.1.0/diff.js'); " +
//                    "const originalText = 'Test1 Test';" +
//                    "const modifiedText = 'Test2 Test';" +
//                    "const diff = Diff.diffWords(originalText, modifiedText);" +
//                    "print(diff);" +
//                    "$response$(diff);";
////                    String jsCode = "const originalText = '\" + t1 + \"'; print('Hello World!');";
//
//            // Execute JavaScript code
//            try {
//                Object result = engine.eval(jsCode);
//                if (result instanceof JsonArray) {
//                    System.out.println(result);
//                }
//            } catch (ScriptException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    @org.junit.Test
//    public void d() throws IOException {
//        Context context = Context.create();
//
//        context.eval(Source.newBuilder("js", new File("C:\\Users\\evapu\\Documents\\GitHub\\QseEvolvingKg\\QseEvolvingKgWebApp\\frontend\\diff.js")).build());
//
//        // Load the JSdiff library asynchronously
////        context.eval("js", "import('https://cdnjs.cloudflare.com/ajax/libs/jsdiff/5.1.0/diff.js').then(module => {" +
////                "  globalThis.Diff = module.Diff;" +
////                "});");
//
//        // Define JavaScript code to use JSdiff after loading
//        String jsCode = "const originalText = 'Test1';" +
//                "const modifiedText = 'Test2';" +
//                "const diff = Diff.diffWords(originalText, modifiedText);" +
//                "diff;";
//
//        // Execute the JavaScript code after the library is loaded
//        Value result = context.eval("js", jsCode);
//        var asdf = 2;
//        String encoded = result.asString();
//        var a = new JsonArray(encoded);
//
//
////        Map<String, Object> resultMap = result.as(Map.class);
////        assertThat(resultMap).hasEntrySatisfying("listProperty", testArray -> {
////            assertThat(testArray).asList().containsExactly("listValue");
////        });
//        // Print the result
////        var test = result.as(JSArray.class);
////        var sdf = test.get("listProperty");
////        if (test instanceof JsonArray) {
////            System.out.println(test);
////        }
//
//        // Close the context
//        context.close();
//    }

}
