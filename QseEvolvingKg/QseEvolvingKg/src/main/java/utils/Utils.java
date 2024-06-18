package utils;

import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static final String shapesPath = "shapes";

    public static Boolean usePrettyFormatting = true; //debugging

    public static String generateTTLFromIRIInModel(IRI iri, Model model) {
        if (usePrettyFormatting) {
            SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
            IRI iriSupport = valueFactory.createIRI("http://shaclshapes.org/support");
            IRI iriConfidence = valueFactory.createIRI("http://shaclshapes.org/confidence");
            var filteredModel = model.stream().filter(statement -> statement.getSubject().equals(iri)).collect(Collectors.toSet());

            var filteredModelWithBlankNodes = addBlankNodesToModel(filteredModel, model);
            filteredModelWithBlankNodes = filteredModelWithBlankNodes.stream().filter(statement -> !statement.getPredicate().equals(iriSupport)
                    && !statement.getPredicate().equals(iriConfidence)).collect(Collectors.toSet());

            //Faster but bug with blank nodes,  TODO investigate
//            org.apache.jena.rdf.model.Model jenaModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
//            filteredModelWithBlankNodes.forEach(statement -> {
//                jenaModel.add(
//                        jenaModel.createResource(statement.getSubject().stringValue()),
//                        jenaModel.createProperty(statement.getPredicate().stringValue()),
//                        jenaModel.createResource(statement.getObject().stringValue())
//                );
//            });


            //need to write to file to load as jena model
            var tmpPath = System.getProperty("user.dir") + File.separator + "tmp.ttl";
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(tmpPath, false);
                Rio.write(filteredModelWithBlankNodes, fileWriter, RDFFormat.TURTLE);
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            org.apache.jena.rdf.model.Model jenaModel = RDFDataMgr.loadModel(tmpPath);

            File file = new File(tmpPath);
            if (file.exists()) {
                file.delete();
            }

            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream outputStream = new ByteArrayOutputStream();
            formatter.accept(jenaModel, outputStream);
            return outputStream.toString().replaceAll("\n+$", "");
        } else {
            StringWriter out = new StringWriter();
            Model filteredModel = model.filter(iri, null, null); //filters current propertyshape
            Rio.write(filteredModel, out, RDFFormat.TURTLE);
            return escapeNew(out.toString());
        }
    }

    public static String generateTTLFromRegex(IRI iri, String fileContent, String prefixLines) {
        //TODO maybe also replace other characters
        String iriWithEscapedChars = iri.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(fileContent);

        if (!matcher.find()) {
            System.out.println("No text generated for " + iri.getLocalName());
            return "";
        }
        String match = matcher.group();

        var model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(prefixLines + match), null, "TURTLE"); // Assuming Turtle format, change as needed

        org.apache.jena.rdf.model.Resource iriSupport = ResourceFactory.createResource("http://shaclshapes.org/support");
        org.apache.jena.rdf.model.Resource iriConfidence = ResourceFactory.createResource("http://shaclshapes.org/confidence");

        String queryString = String.format("CONSTRUCT {?s ?p ?o} WHERE { ?s ?p ?o. FILTER (?p != <%s> && ?p != <%s>)}", iriSupport, iriConfidence);

        var query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream outputStream = new ByteArrayOutputStream();
            formatter.accept(jenaModel, outputStream);
            String cleanedString = reorderShaclInItems(outputStream.toString());
            String cleanedStringOrItems = reOrderOrItems(cleanedString);
            return cleanedStringOrItems.replaceAll("\n+$", "");
        }
    }

    private static String reorderShaclInItems(String input) {
        String searchString = "shacl#in";
        if (input.contains(searchString) && input.indexOf(searchString) != input.lastIndexOf(searchString)) {
            String[] lines = input.split("\n");
            List<String> inLines = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(searchString))
                    inLines.add(line.trim());
            }
            Collections.sort(inLines);
            List<String> orderedLines = new ArrayList<>();
            int remainingIndex = 0;
            for (String line : lines) {
                if (line.contains(searchString)) {
                    orderedLines.add(inLines.get(remainingIndex));
                    remainingIndex++;
                } else
                    orderedLines.add(line);
            }
            StringBuilder orderedText = new StringBuilder();
            orderedText.append(String.join("\n", orderedLines));

            return orderedText.toString();
        } else
            return input;
    }

    //not used. Alternative to filtering with rdf4j-model, writing to file and reading jena file from file.
    //Not tested
    public static String generateTTLFromIRIInModelJena(IRI iri, org.apache.jena.rdf.model.Model model) {
        org.apache.jena.rdf.model.Resource iriSupport = ResourceFactory.createResource("http://shaclshapes.org/support");
        org.apache.jena.rdf.model.Resource iriConfidence = ResourceFactory.createResource("http://shaclshapes.org/confidence");
        String iriWithEscapedChars = iri.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");

        String queryString = String.format("CONSTRUCT {?s ?p ?o} WHERE { " +
                "    <%s> (rdf:type|!rdf:type)* ?s ." +
                "    ?s ?p ?o " +
                "    FILTER ((?s = <%s> || isBlank(?s)) && ?p != <%s> && ?p != <%s>) " +
                "    }", iriWithEscapedChars, iriWithEscapedChars, iriSupport, iriConfidence);

        var query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            org.apache.jena.rdf.model.Model jenaModel = qexec.execConstruct();
            TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
            OutputStream outputStream = new ByteArrayOutputStream();
            formatter.accept(jenaModel, outputStream);
            return outputStream.toString().replaceAll("\n+$", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String reOrderOrItems(String input) {
        try {
            String orItemString = "<http://www.w3.org/ns/shacl#or>"; //highly dependent on turtlePrettyFormatter
            String patternString = orItemString + " \\([^\\)]*\\) ;"; //would not work for names with '('
            Pattern patternOrParent = Pattern.compile(patternString, Pattern.DOTALL);
            Matcher matcherOrParent = patternOrParent.matcher(input);
            var inputCopy = input;
            List<String> orObjects = new ArrayList<>();
            while(matcherOrParent.find()) {
                var firstResultParent = matcherOrParent.group();
                var patternOrObjects = Pattern.compile("\\[[^\\]]*\\]", Pattern.DOTALL);
                var matcherObjects = patternOrObjects.matcher(firstResultParent);
                List<String> objects = new ArrayList<>();
                while (matcherObjects.find()) {
                    String object = matcherObjects.group().trim(); // Extract contents of brackets and trim whitespace
                    objects.add(object);
                }
                objects.sort(Comparator.comparing(o -> o));
                var newString = firstResultParent;
                StringBuilder newOrItems = new StringBuilder();
                for (var m : objects) {
                    newString = newString.replace(m, "");
                    newOrItems.append(m).append(" ");
                }
                var newOrItemString = insertAfter(newString, orItemString + " ( ", newOrItems.toString());
                inputCopy = inputCopy.replace(firstResultParent, newOrItemString);
                orObjects.add(newOrItemString);
            }

            //reorder or-objects in general (in case of multiple
            orObjects.sort(Comparator.comparing(o -> o));
            StringBuilder newOrItems = new StringBuilder();
            for (var m : orObjects) {
                inputCopy = inputCopy.replace(m, "");
                newOrItems.append(m).append(" \r\n");
            }
            int index = input.indexOf(orItemString);
            if (index == -1)
                return input;
            inputCopy = insertAfter(inputCopy, input.substring(0, index), newOrItems.toString());
            return inputCopy;
        } catch (Exception ex) {
            ex.printStackTrace();
            return input;
        }
    }

    public static String insertAfter(String original, String searchString, String toInsert) {
        int index = original.indexOf(searchString);
        if (index == -1) {
            return original;
        }
        String part1 = original.substring(0, index + searchString.length());
        String part2 = original.substring(index + searchString.length()).trim();

        return part1 + toInsert + part2;
    }

    public static String escapeNew(String input) {
        if (usePrettyFormatting) {
            return input.replaceAll("\r", "").replaceAll("\n", "\\\\n");
        } else {
            input = input.replaceFirst("\r\n", "");
            return input.replaceAll("\r\n", "\\\\\\\\n");
        }
    }

    private static Set<Statement> addBlankNodesToModel(Set<Statement> filteredModel, Model model) {
        var blankNodeQueue = filteredModel.stream().filter(statement -> statement.getObject() instanceof BNode).collect(Collectors.toList());
        while (blankNodeQueue.size() != 0) {
            var nextStatement = blankNodeQueue.get(0);
            var modelToAdd = model.stream().filter(statement -> statement.getSubject().equals(nextStatement.getObject())).toList();
            filteredModel.addAll(modelToAdd);
            blankNodeQueue.remove(nextStatement);
            var tmp = modelToAdd.stream().filter(statement -> statement.getObject() instanceof BNode).toList();
            blankNodeQueue.addAll(tmp);
        }
        return filteredModel;
    }
    public static String getGraphDirectory() {
        String projectDirectory = System.getProperty("user.dir");
        projectDirectory = projectDirectory + File.separator + "graphs" + File.separator;
        return projectDirectory;
    }
}