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

    public String deleteFromFileWhereSupportIsZero(String filePath, List<NodeShape> nodeShapes) {
        String fileContent = getFileAsString(filePath);

        for (var nodeShape : nodeShapes) {
            if(nodeShape.support == 0) {
                fileContent = deleteIriFromString(nodeShape.iri.toString(), fileContent, nodeShape.errorDuringGeneration);
                for (var propertyShape : nodeShape.propertyShapes) {
                    fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                }
            }
            else {
                for(var propertyShape : nodeShape.propertyShapes) {
                    if(propertyShape.support == 0 && propertyShape.orItems == null) {
                        fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                        fileContent = deletePropertyShapeReferenceWithIriFromString(propertyShape.iri.toString(), fileContent, propertyShape.errorDuringGeneration);
                    }
                    else {
                        //TODO does not work
//                        for(var orItem : propertyShape.orItems) {
//                            if(orItem.support == 0) {
//                                fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent);
//                            }
//                        }
                    }
                }
            }

        }
        return fileContent;
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
        String iriWithEscapedChars = iri.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.\n", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + iri.toString());
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

    public String deleteShaclOrItemWithIriFromString(ShaclOrListItem orItem, String shape, boolean errorDuringGeneration) {
        if(errorDuringGeneration)
            return shape;

        String regexPart = "";
        if(orItem.dataType != null)
            regexPart = String.format(" \\<http://www.w3.org/ns/shacl#datatype> <?%s>?", orItem.dataType);
        else if(orItem.classIri != null)
            regexPart = String.format(" \\<http://www.w3.org/ns/shacl#class> <?%s>?", orItem.classIri);

        String regexPattern = String.format(" \\[.*?<http://www.w3.org/ns/shacl#NodeKind> <%s>.*?%s.*?\\] ", orItem.nodeKind, regexPart);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(shape);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + orItem.toString());
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
