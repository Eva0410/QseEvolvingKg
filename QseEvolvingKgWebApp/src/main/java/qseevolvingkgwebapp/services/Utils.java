package qseevolvingkgwebapp.services;

import com.vaadin.flow.component.select.Select;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.views.comparisondetails.ComparisonDetailsView;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    static String graphDirectory;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static String getGraphDirectory() {
        String projectDirectory = System.getProperty("user.dir");
        projectDirectory = projectDirectory + File.separator + "graphs" + File.separator;
        return projectDirectory;
    }

    public static List<ComboBoxItem> getAllGraphs(GraphService graphService) {
        return graphService.listAll().stream()
                .map(graph -> new Utils.ComboBoxItem(graph.getName(),graph.getId()))
                .collect(Collectors.toList());
    }

    public static List<ComboBoxItem> getAllVersions(VersionService versionService, Long graphId) {
        return versionService.listByGraphId(graphId).stream()
                .map(version -> new Utils.ComboBoxItem(version.getVersionNumber() + " - " + version.getName(),version.getId()))
                .collect(Collectors.toList());
    }

    public static class ComboBoxItem {
        public String label;
        public Long id;

        public ComboBoxItem(String label, Long id) {
            this.label = label;
            this.id = id;
        }

        public ComboBoxItem() {

        }
    }

    public static Select<ComboBoxItem> setComboBoxGraphData(GraphService graphService, Select<ComboBoxItem> selectItemGraph) {
        List<Utils.ComboBoxItem> graphs = Utils.getAllGraphs(graphService);
        selectItemGraph.setItems(graphs);
        selectItemGraph.setItemLabelGenerator(item -> item.label);
        if (graphs.size() > 0) {
            var firstItem = graphs.stream().findFirst();
            selectItemGraph.setValue(firstItem.get());
        }
        return selectItemGraph;
    }

    public static Select<ComboBoxItem> setComboBoxVersionsData(Long graphId, VersionService versionService, Select<ComboBoxItem> selectItemVersion, boolean setFirstVersion) {
        List<Utils.ComboBoxItem> versions = Utils.getAllVersions(versionService, graphId);
        selectItemVersion.setItems(versions);
        selectItemVersion.setItemLabelGenerator(item -> item.label);
        if (versions.size() > 0 && setFirstVersion) {
            var firstItem = versions.stream().findFirst();
            selectItemVersion.setValue(firstItem.get());
        }
        return selectItemVersion;
    }

    public static Boolean usePrettyFormatting = true; //debugging
    public static String generateTTLFromIRIInModel(IRI iri, Model model) {
        if(usePrettyFormatting) {
            var filteredModel = model.stream().filter(statement -> statement.getSubject().equals(iri)).collect(Collectors.toSet());
            var filteredModelWithBlankNodes = addBlankNodesToModel(filteredModel, model);

            //need to write to file to load as jena model
            var tmpPath = System.getProperty("user.dir")+"\\tmp.ttl";
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
        }
        else {
            StringWriter out = new StringWriter();
            Model filteredModel = model.filter(iri, null, null); //filters current propertyshape
            Rio.write(filteredModel, out, RDFFormat.TURTLE);
            return escapeNew(out.toString());
        }
    }

    public static String escapeNew(String input) {
        if(usePrettyFormatting) {
            return input.replaceAll("\r","").replaceAll("\n","\\\\\\\\n");
        }
        else {
            input = input.replaceFirst("\r\n", "");
            return input.replaceAll("\r\n", "\\\\\\\\n");
        }
    }

    private static Set<Statement> addBlankNodesToModel(Set<Statement> filteredModel, Model model) {
        var blankNodeQueue = filteredModel.stream().filter(statement -> statement.getObject() instanceof BNode).collect(Collectors.toList());
        while(blankNodeQueue.size() != 0) {
            var nextStatement = blankNodeQueue.get(0);
            var modelToAdd = model.stream().filter(statement -> statement.getSubject().equals(nextStatement.getObject())).collect(Collectors.toList());
            filteredModel.addAll(modelToAdd);
            blankNodeQueue.remove(nextStatement);
            var tmp = modelToAdd.stream().filter(statement -> statement.getObject() instanceof BNode).collect(Collectors.toList());
            blankNodeQueue.addAll(tmp);
        }
        return filteredModel;
    }

    public static String getComboBoxLabelForExtractedShapes(ExtractedShapes shape) {
        var comboBoxItem = new Utils.ComboBoxItem();
        comboBoxItem.id = shape.getId();
        var version = shape.getVersionEntity();
        var graph = version.getGraph();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return graph.getName() + "-" + version.getVersionNumber() + "-" + version.getName() + "-"
                + formatter.format(shape.getCreatedAt()) + "-"
                + shape.getQseType() + "-" + shape.getSupport() + "-" + shape.getConfidence();
    }
}