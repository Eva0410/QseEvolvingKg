package qseevolvingkg.partialsparqlqueries;

import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    public void getAllNodeShapesfromFile(String filePath) {
        String regexPattern = String.format("\n.* <http:\\/\\/www.w3.org\\/ns\\/shacl#NodeShape> ;");
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(getFileAsString(filePath));
        List<String> matches = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();
            String nodeShapeName = match.split(">")[0] + ">";
            matches.add(nodeShapeName);
        }
    }

    public static String deleteFromFileWhereSupportIsZero(ExtractedShapes extractedShapes, ComparisonDiff comparisonDiff) {
        String fileContent = extractedShapes.getFileAsString();

        for (var nodeShape : extractedShapes.getNodeShapes()) {
            if(nodeShape.support == 0) {
                comparisonDiff.deletedNodeShapes.add(nodeShape.iri.toString());
                fileContent = deleteIriFromString(nodeShape.iri.toString(), fileContent, nodeShape.errorDuringGeneration);
                for (var propertyShape : nodeShape.propertyShapes) {
                    comparisonDiff.deletedPropertShapes.add(propertyShape.iri.toString());
                    fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                }
            }
            else {
                for(var propertyShape : nodeShape.propertyShapes) {
                    var allOrItemsSupportZero = propertyShape.orItems != null && propertyShape.orItems.stream().mapToInt(o -> o.support).sum() == 0;
                    if((propertyShape.support == 0 && propertyShape.orItems == null) || allOrItemsSupportZero) {
                        comparisonDiff.deletedPropertShapes.add(propertyShape.iri.toString());
                        fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                        fileContent = deletePropertyShapeReferenceWithIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                    }
                    else if(propertyShape.support != 0 && propertyShape.orItems != null) {
                        var numberOfOrItemsLeft = propertyShape.orItems.stream().filter(o -> o.support != 0).count();
                        String originalShape = getShapeAsString(propertyShape.iri.toString(), fileContent);
                        String modifiedShape = originalShape;
                        for(var orItem : propertyShape.orItems) {
                            if(orItem.support == 0) {
                                modifiedShape = deleteShaclOrItemWithIriFromString(orItem, modifiedShape, false);
                            }
                        }
                        if(numberOfOrItemsLeft == 1 && propertyShape.orItems.size() != 1) {
                            int newSupport = propertyShape.orItems.stream().filter(o -> o.support != 0).findFirst().get().support;

                            modifiedShape = extractedShapes.prefixLines + modifiedShape;
                            modifiedShape = GraphDbUtils.deleteOrListAndConnectToParentNode(modifiedShape, propertyShape.iri.toString(), newSupport);
                            modifiedShape = RegexUtils.removeLinesWithPrefix(modifiedShape);
                        }
                        fileContent = fileContent.replace(originalShape, modifiedShape);
                    }
                }
            }

        }
        return fileContent;
    }

    public static String removeLinesWithPrefix(String input) {
        StringBuilder result = new StringBuilder();
        String[] lines = input.split("\\r?\\n");

        for (String line : lines) {
            if (!line.startsWith("@prefix")) {
                result.append(line).append("\r\n");
            }
        }

        //remove leading new lines
        return result.toString().replaceFirst("^\\n+", "");
    }

    public static void saveStringAsFile(String content, String filePath) {
        byte[] bytes = content.getBytes();

        try {
            Path path = Paths.get(filePath);
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save the file: " + e.getMessage());
        }
    }

    public static void copyFile(String sourceFilePath, String destinationFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        Path destinationPath = Paths.get(destinationFilePath);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to copy the file: " + e.getMessage());
        }
    }

    private static String deleteIriFromString(String iri, String file, boolean errorDuringGeneration) {
        if(errorDuringGeneration)
            return file;
        String iriWithEscapedChars = iri.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + iri);
        }
        String match = matcher.group();
        return file.replace(match, "");
    }

    private static String deletePropertyShapeReferenceWithIriFromString(String iri, String file, boolean errorDuringGeneration) {
        if(errorDuringGeneration)
            return file;
        String iriWithEscapedChars = iri.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("  <http://www.w3.org/ns/shacl#property> <%s> \\;\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + iri);
        }
        String match = matcher.group();
        return file.replace(match, "");
    }

    //only works if at least one or item stays
    public static String deleteShaclOrItemWithIriFromString(ShaclOrListItem orItem, String shape, boolean errorDuringGeneration) {
        if(errorDuringGeneration)
            return shape;

        String regexPart = "";
        if (orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#Literal"))
            regexPart = String.format(" \\<http://www.w3.org/ns/shacl#datatype> <?%s>?", orItem.dataType);
        else if(orItem.nodeKind.toString().equals("http://www.w3.org/ns/shacl#IRI"))
            regexPart = String.format(" \\<http://www.w3.org/ns/shacl#class> <?%s>?", orItem.classIri);

        String regexPattern = String.format(" \\[[^\\[\\]]*?<http://www.w3.org/ns/shacl#NodeKind> <%s>[^\\[\\]]*?%s[^\\]\\[]*?\\]", orItem.nodeKind, regexPart);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(shape);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + orItem.propertyShape.iri.toString() + ", " + orItem.toString());
            return shape;
        }
        String match = matcher.group();
        return shape.replace(match, "");
    }

    public static String getFileAsString(String path) {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }

    public static String getShapeAsString(String iri, String file) {
        String iriWithEscapedChars = iri.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Could not find shape " + iri);
        }
        return matcher.group();
    }

    //TODO copied from webapp
    public static String getShapeAsStringFormatted(String iri, String file, String prefixLines) {
        String iriWithEscapedChars = iri.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Could not find shape " + iri);
        }
        String match = matcher.group();
        var model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        model.read(new java.io.StringReader(prefixLines + match), null, "TURTLE");

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

    public static String insertAfter(String original, String searchString, String toInsert) {
        int index = original.indexOf(searchString);
        if (index == -1) {
            return original;
        }
        String part1 = original.substring(0, index + searchString.length());
        String part2 = original.substring(index + searchString.length()).trim();

        return part1 + toInsert + part2;
    }

}
