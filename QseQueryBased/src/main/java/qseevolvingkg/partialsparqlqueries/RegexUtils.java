package qseevolvingkg.partialsparqlqueries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
