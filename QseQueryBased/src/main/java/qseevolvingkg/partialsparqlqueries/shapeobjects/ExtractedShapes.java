package qseevolvingkg.partialsparqlqueries.shapeobjects;

import cs.qse.common.structure.NS;
import qseevolvingkg.partialsparqlqueries.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//Copied from WebApp
public class ExtractedShapes {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public int support = 0;
    public double confidence = 0;

    public String fileContentPath;

    public List<NodeShape> nodeShapes;

    public String fileAsString;

    public String prefixLines;

    public String getFileAsString() {
        StringBuilder fileContent = new StringBuilder();
        StringBuilder prefixLines = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileContentPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
                if(line.contains("@prefix"))
                    prefixLines.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to read file: " + e.getMessage());
        }
        this.fileAsString = fileContent.toString().replace("\r", "").replace("\r\n", "\n");
        this.prefixLines = prefixLines.toString();
        return fileAsString;
    }

    public List<NodeShape> getNodeShapes() {
        return nodeShapes;
    }

    private Boolean nsAlreadyExists(ArrayList<NodeShape> list, NS item) {
        return list.stream().anyMatch(li -> li.iri.equals(item.getIri()));
    }

    public void setNodeShapes(List<NS> ns) {
        var list = new ArrayList<NodeShape>();
        for(var item : ns) {
            //Bug in QSE...
            var nsAlreadyExists = nsAlreadyExists(list, item);
            if(item.getSupport() > this.support && !nsAlreadyExists) {
                list.add(new NodeShape(item, this));
            }
        }
        this.nodeShapes = list;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
