package qseevolvingkg.partialsparqlqueries;

import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.query.QueryLanguage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public String deleteFromRepoWhereSupportIsZero(String filePath, List<NodeShape> nodeShapes) {
        String fileContent = getFileAsString(filePath);
        var filteredNodeShapes = nodeShapes.stream().filter(ns -> ns.support == 0).toList();

        for (var nodeShape : filteredNodeShapes) {
            fileContent = deleteIriFromString(nodeShape.iri.toString(), fileContent);
            for (var propertyShape : nodeShape.propertyShapes) {
                fileContent = deleteIriFromString(propertyShape.iri.toString(), fileContent);
            }
        }
        return fileContent; //Todo testen
    }

    private String deleteIriFromString(String iri, String file) {
        String iriWithEscapedChars = iri.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
        String regexPattern = String.format("\n<%s>.*? \\.", iriWithEscapedChars);
        Pattern pattern = Pattern.compile(regexPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(file);
        if (!matcher.find()) {
            System.out.println("Delete did not work for " + iri.toString());
        }
        String match = matcher.group();
        return file.replace(match, "");

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
}
