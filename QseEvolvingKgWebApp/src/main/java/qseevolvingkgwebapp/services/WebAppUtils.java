package qseevolvingkgwebapp.services;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.component.upload.Upload;
import data.ExtractedShapes;
import data.Graph;
import data.Version;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebAppUtils {

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String preconfiguredFolderName = "pre_configured";

    public static List<ComboBoxItem> getAllGraphs(GraphService graphService) {
        return graphService.listAll().stream()
                .map(graph -> new WebAppUtils.ComboBoxItem(graph.getName(), graph.getId()))
                .collect(Collectors.toList());
    }

    public static List<ComboBoxItem> getAllVersions(VersionService versionService, Long graphId) {
        return versionService.listByGraphId(graphId).stream()
                .map(version -> new WebAppUtils.ComboBoxItem(version.getVersionNumber() + " - " + version.getName(), version.getId()))
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
        List<WebAppUtils.ComboBoxItem> graphs = WebAppUtils.getAllGraphs(graphService);
        selectItemGraph.setItems(graphs);
        selectItemGraph.setItemLabelGenerator(item -> item.label);
        var selectedGraphId = (Long) VaadinSession.getCurrent().getAttribute("shapes_currentGraphId");
        var firstItem = graphs.stream().findFirst();

        if (selectedGraphId != null) {
            var graphItem = selectItemGraph.getDataProvider().fetch(new Query<>()).filter(g -> g.id.equals(selectedGraphId)).findFirst();
            if (graphItem.isPresent())
                selectItemGraph.setValue(graphItem.get());
            else if (firstItem.isPresent())
                selectItemGraph.setValue(firstItem.get());
        } else if (firstItem.isPresent())
            selectItemGraph.setValue(firstItem.get());
    }

    public static void setComboBoxVersionsData(Long graphId, VersionService versionService, Select<ComboBoxItem> selectItemVersion) {
        List<WebAppUtils.ComboBoxItem> versions = WebAppUtils.getAllVersions(versionService, graphId);
        selectItemVersion.setItems(versions);
        selectItemVersion.setItemLabelGenerator(item -> item.label);
        var currentVersionId = (Long) VaadinSession.getCurrent().getAttribute("shapes_currentVersionId");
        var firstItem = versions.stream().findFirst();

        if (currentVersionId != null) {
            var graphItem = selectItemVersion.getDataProvider().fetch(new Query<>()).filter(v -> v.id.equals(currentVersionId)).findFirst();
            if (graphItem.isPresent())
                selectItemVersion.setValue(graphItem.get());
            else if (firstItem.isPresent())
                selectItemVersion.setValue(firstItem.get());
        } else if (firstItem.isPresent())
            selectItemVersion.setValue(firstItem.get());
    }

    public static void handleSaveFile(Graph graph, VersionService versionService, InputStream inputStream, String versionName, String preConfiguredGraphPath) {
        Version version = versionService.generateNewVersion(graph);
        if (preConfiguredGraphPath.isEmpty()) {
            var dir = Utils.getGraphDirectory();
            String directory = dir + graph.getName() + File.separator;
            String generatedFileName = graph.getName() + "_" + version.getVersionNumber() + ".nt";
            String filePath = directory + generatedFileName;
            version.setPath(filePath);
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            version.setPath(preConfiguredGraphPath);
        }
        version.setName(versionName);
        versionService.update(version);
    }

    public static void setGraphOrVersionGuiFields(TextField textFieldGraphName, Button buttonSave, Upload uploadGraphFile, Select<String> preconfiguredGraphs) {
        textFieldGraphName.setHeight("min-content");

        textFieldGraphName.setLabel("Name");
        textFieldGraphName.setWidth("min-content");
        buttonSave.setWidth("min-content");
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        textFieldGraphName.setRequiredIndicatorVisible(true);
        uploadGraphFile.setUploadButton(new Button("Or upload .nt file"));
        buttonSave.setTooltipText("This will copy the file to the project directory");

        uploadGraphFile.setAcceptedFileTypes(".nt");

        var files = WebAppUtils.listFilesInStaticGraphDirectory();
        preconfiguredGraphs.setItems(files);
        preconfiguredGraphs.setItemLabelGenerator(item -> item == null ? "" : item.substring(item.indexOf(WebAppUtils.preconfiguredFolderName) + WebAppUtils.preconfiguredFolderName.length() + 1));
        preconfiguredGraphs.setLabel("Select pre-configured graph");
        preconfiguredGraphs.setEmptySelectionAllowed(true);
    }

    public static Boolean isEmptyItemSelected(Select<String> preconfiguredGraphs) {
        var vasdf = preconfiguredGraphs.getValue();
        return preconfiguredGraphs.getValue() == null || preconfiguredGraphs.getValue().isEmpty();
    }

    public static String getComboBoxLabelForExtractedShapes(ExtractedShapes shape) {
        if (shape.getComboBoxString().isEmpty())
            shape.generateComboBoxString();
        return shape.getComboBoxString();
    }

    public static List<String> listFilesInStaticGraphDirectory() {
        Path projectDirectory = Paths.get("").toAbsolutePath().resolve("graphs" + File.separator + preconfiguredFolderName);
        try {
            if (!Files.exists(projectDirectory))
                Files.createDirectory(projectDirectory);
            return Files.walk(projectDirectory)
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".nt"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
