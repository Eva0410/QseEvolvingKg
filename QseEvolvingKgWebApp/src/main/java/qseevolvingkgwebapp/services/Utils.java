package qseevolvingkgwebapp.services;

import com.vaadin.flow.component.select.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    static String graphDirectory;

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
}