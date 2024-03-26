package qseevolvingkgwebapp.services;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.server.VaadinSession;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.Version;

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

    public static void setComboBoxGraphData(GraphService graphService, Select<ComboBoxItem> selectItemGraph) {
        List<Utils.ComboBoxItem> graphs = Utils.getAllGraphs(graphService);
        selectItemGraph.setItems(graphs);
        selectItemGraph.setItemLabelGenerator(item -> item.label);
        var selectedGraphId = (Long) VaadinSession.getCurrent().getAttribute("shapes_currentGraphId");
        var firstItem = graphs.stream().findFirst();


        if(selectedGraphId != null) {
            var graphItem = selectItemGraph.getDataProvider().fetch(new Query<>()).filter(g -> g.id.equals(selectedGraphId)).findFirst();
            if(graphItem.isPresent())
                selectItemGraph.setValue(graphItem.get());
            else
                if(firstItem.isPresent())
                    selectItemGraph.setValue(firstItem.get());
        }
        else
            if(firstItem.isPresent())
                selectItemGraph.setValue(firstItem.get());
    }

    public static void setComboBoxVersionsData(Long graphId, VersionService versionService, Select<ComboBoxItem> selectItemVersion) {
        List<Utils.ComboBoxItem> versions = Utils.getAllVersions(versionService, graphId);
        selectItemVersion.setItems(versions);
        selectItemVersion.setItemLabelGenerator(item -> item.label);
        var currentVersionId = (Long) VaadinSession.getCurrent().getAttribute("shapes_currentVersionId");
        var firstItem = versions.stream().findFirst();

        if(currentVersionId != null) {
            var graphItem = selectItemVersion.getDataProvider().fetch(new Query<>()).filter(v -> v.id.equals(currentVersionId)).findFirst();
            if(graphItem.isPresent())
                selectItemVersion.setValue(graphItem.get());
            else
            if(firstItem.isPresent())
                selectItemVersion.setValue(firstItem.get());
        }
        else
        if(firstItem.isPresent())
            selectItemVersion.setValue(firstItem.get());
    }

    public static Boolean usePrettyFormatting = true; //debugging
    public static String generateTTLFromIRIInModel(IRI iri, Model model) {
        if(usePrettyFormatting) {
            var filteredModel = model.stream().filter(statement -> statement.getSubject().equals(iri)).collect(Collectors.toSet());
            SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
            IRI iriSupport = valueFactory.createIRI("http://shaclshapes.org/support");
            IRI iriConfidence = valueFactory.createIRI("http://shaclshapes.org/confidence");

            var filteredModelWithBlankNodes = addBlankNodesToModel(filteredModel, model);
            filteredModelWithBlankNodes = filteredModelWithBlankNodes.stream().filter(statement -> !statement.getPredicate().equals(iriSupport)
                    && !statement.getPredicate().equals(iriConfidence)).collect(Collectors.toSet());

            //need to write to file to load as jena model
            var tmpPath = System.getProperty("user.dir")+File.separator+"tmp.ttl";
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
            var modelToAdd = model.stream().filter(statement -> statement.getSubject().equals(nextStatement.getObject())).toList();
            filteredModel.addAll(modelToAdd);
            blankNodeQueue.remove(nextStatement);
            var tmp = modelToAdd.stream().filter(statement -> statement.getObject() instanceof BNode).toList();
            blankNodeQueue.addAll(tmp);
        }
        return filteredModel;
    }

    public static String getComboBoxLabelForExtractedShapes(ExtractedShapes shape) {
        if(shape.getComboBoxString().isEmpty())
            shape.generateComboBoxString();
        return shape.getComboBoxString();
    }

    public static void handleSaveFile(String graphName, VersionService versionService, InputStream inputStream, String versionName) {
        Version version = versionService.generateNewVersion(graph);
        var dir = Utils.getGraphDirectory();
        String directory = dir+graphName+File.separator;
        String generatedFileName = graphName + "_" + version.getVersionNumber() +".nt";
        String filePath = directory+generatedFileName;
        version.setPath(filePath);
        version.setName(versionName);
        versionService.update(version);
        File file = new File(directory);

        if (!file.exists()) {
            file.mkdirs();
        }

        File outputFile = new File(directory, generatedFileName);

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}