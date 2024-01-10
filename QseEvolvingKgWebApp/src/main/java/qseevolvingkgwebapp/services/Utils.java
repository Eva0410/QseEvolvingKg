package qseevolvingkgwebapp.services;

import com.vaadin.flow.component.select.Select;
import cs.qse.common.TurtlePrettyFormatter;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.ExtractedShapes;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public static String generateTTLFromIRIInModel(IRI iri, Model model) {
        StringWriter out = new StringWriter();
        Model filteredModel = model.filter(iri, null, null);
        Rio.write(filteredModel, out, RDFFormat.TURTLE);
        //TODO pretty format!
//        TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
//        org.apache.jena.rdf.model.Model jenaModel = RDFDataMgr.loadModel(filteredModel);
//        filteredModel.forEach(stmt -> jenaModel.add(convert(stmt)));
//
//        String output = formatter.apply(jenaModel);

        return out.toString();
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