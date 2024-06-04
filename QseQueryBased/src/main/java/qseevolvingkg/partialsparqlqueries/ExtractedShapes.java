package qseevolvingkg.partialsparqlqueries;

import cs.qse.common.structure.NS;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

//todo use version from web-app?
public class ExtractedShapes {
    int support;
    double confidence;

    List<String> classes;

    public String fileContentPath;

    String fileContentDefaultShapesPath;

    List<NodeShape> nodeShapes;

    List<NodeShape> nodeShapesDefault;

    Model model;
    org.apache.jena.rdf.model.Model jenaModel;


    //for regex
    String fileAsString;


    public String prefixLines;

    //not used, would be used for alternative with rdf4j model and jena model
    public Model getModel() {
        if(model == null) {
            try(FileInputStream inputStream = new FileInputStream(fileContentPath)) {
                this.model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.model;
    }

    //not used, needed for alternative with Jena Model
    public org.apache.jena.rdf.model.Model getModelJena() {
        if(jenaModel == null) {
            try(FileInputStream inputStream = new FileInputStream(fileContentPath)) {
                var jenaModel = ModelFactory.createDefaultModel();
                RDFDataMgr.read(jenaModel, inputStream, RDFLanguages.TTL);
                this.jenaModel = jenaModel;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.jenaModel;
    }

    public String getFileAsString() {
        if(fileAsString == null) {
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
                e.printStackTrace();
            }
            this.fileAsString = fileContent.toString();
            this.prefixLines = prefixLines.toString();
        }
        return fileAsString;
    }

    public List<NodeShape> getNodeShapes() {
        return nodeShapes;
    }

    private Boolean nsAlreadyExists(ArrayList<NodeShape> list, NS item) {
        return list.stream().anyMatch(li -> li.iri.equals(item.getIri()));
    }

    public String getClassesAsString() {
        if (classes != null && !classes.isEmpty()) {
            var shortenedList = new ArrayList<>(classes);
            for (int i = 0; i < classes.size(); i++) {
                if(shortenedList.get(i).contains("#"))
                    shortenedList.set(i, shortenedList.get(i).split("#")[1]);
                else
                    shortenedList.set(i, shortenedList.get(i));
            }
            return shortenedList.stream().sorted().collect(Collectors.joining(", "));
        }
        return "";
    }

    public void setNodeShapes(List<NS> ns) {
        var list = new ArrayList<NodeShape>();
        for(var item : ns) {
            //Bug in QSE...
            var nsAlreadyExists = nsAlreadyExists(list, item);
            if(item.getSupport() > this.support && !nsAlreadyExists)
                list.add(new NodeShape(item));
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

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public boolean isDefaultshape() {
        return confidence == 0 && support == 0;
    }

    public List<NodeShape> getNodeShapesDefault() {
        return nodeShapesDefault;
    }

    public String getFileContentPath() {
        return fileContentPath;
    }

    public String getFileContentDefaultShapesPath() {
        return fileContentDefaultShapesPath;
    }
}
