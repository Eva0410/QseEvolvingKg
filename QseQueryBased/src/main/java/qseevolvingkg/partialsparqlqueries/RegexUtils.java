package qseevolvingkg.partialsparqlqueries;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
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

    public String deleteFromFileWhereSupportIsZero(ExtractedShapes extractedShapes) {
        String fileContent = extractedShapes.getFileAsString();

        for (var nodeShape : extractedShapes.getNodeShapes()) {
            if(nodeShape.support == 0) {
                fileContent = deleteIriFromString(nodeShape.iri.toString(), fileContent, nodeShape.errorDuringGeneration);
                for (var propertyShape : nodeShape.propertyShapes) {
                    fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                }
            }
            else {
                for(var propertyShape : nodeShape.propertyShapes) {
                    var allOrItemsSupportZero = propertyShape.orItems != null && propertyShape.orItems.stream().mapToInt(o -> o.support).sum() == 0;
                    if((propertyShape.support == 0 && propertyShape.orItems == null) || allOrItemsSupportZero) {
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

    public void saveStringAsFile(String content, String filePath) {
        byte[] bytes = content.getBytes();

        try {
            Path path = Paths.get(filePath);
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save the file: " + e.getMessage());
        }
    }

    public void copyFile(String sourceFilePath, String destinationFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        Path destinationPath = Paths.get(destinationFilePath);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to copy the file: " + e.getMessage());
        }
    }

    private String deleteIriFromString(String iri, String file, boolean errorDuringGeneration) {
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

    private String deletePropertyShapeReferenceWithIriFromString(String iri, String file, boolean errorDuringGeneration) {
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
    public String deleteShaclOrItemWithIriFromString(ShaclOrListItem orItem, String shape, boolean errorDuringGeneration) {
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

    public String getFileAsString(String path) {
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

    public String getShapeAsString(String iri, String file) {
        String iriWithEscapedChars = iri.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Could not find shape " + iri);
        }
        return matcher.group();
    }

}
