package qseevolvingkgwebapp.views.generateshapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import cs.Main;
import cs.qse.common.encoders.StringEncoder;
import cs.qse.common.structure.NS;
import cs.qse.filebased.Parser;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.ShactorUtils.PruningUtil;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.data.QseType;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.services.Utils.ComboBoxItem;
import qseevolvingkgwebapp.views.MainLayout;
import qseevolvingkgwebapp.views.shapes.ShapesView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Generate Shapes")
@Route(value = "generate-shapes", layout = MainLayout.class)
@Uses(Icon.class)
public class GenerateShapesView extends Composite<VerticalLayout> implements HasUrlParameter<Long> {

    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;

    @Autowired()
    private ShapesService shapeService;

    Select<ComboBoxItem> comboBoxGraph;
    Select<ComboBoxItem> comboBoxVersion;
    Long currentVersionId;
    Grid classesGrid;
    Parser parser;
    H5 graphInfo;
    TextField searchField;
    List<String> chosenClasses;
    Set<Integer> chosenClassesEncoded;
    Button buttonPrimary;
    String defaultShapesOutputFileAddress = "";
    String prunedFileAddress = "";
    Checkbox checkbox;
    NumberField confidence;
    IntegerField support;

    HashMap<String, String> defaultShapesModelStats;
    Graph currentGraph;
    Version currentVersion;
    RadioButtonGroup radioGroupQseType;



    public GenerateShapesView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow.setSpacing(true);
        layoutRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        comboBoxGraph = new Select<>();
        comboBoxVersion = new Select<>();
        radioGroupQseType = new RadioButtonGroup();
        classesGrid = new Grid(Type.class,false);
        support = new IntegerField();
        confidence = new NumberField();
        checkbox = new Checkbox();
        checkbox.setLabel("Use default shapes");
        checkbox.setValue(true);
        buttonPrimary = new Button();
        graphInfo = new H5();
        searchField = new TextField();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        comboBoxGraph.setLabel("Graph");
        comboBoxGraph.setWidth("min-content");
        comboBoxVersion.setLabel("Version");
        comboBoxVersion.setWidth("min-content");
        radioGroupQseType.setLabel("QSE-Type");
        radioGroupQseType.setWidth("min-content");
        radioGroupQseType.setItems(Arrays.stream(QseType.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        radioGroupQseType.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupQseType.setValue(QseType.EXACT.toString());
        classesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        classesGrid.setWidth("100%");
        classesGrid.getStyle().set("flex-grow", "0");
        classesGrid.addColumn("name").setHeader("Class IRI").setSortable(true);
        classesGrid.addColumn("instanceCount").setHeader("Class Instance Count").setSortable(true);
        classesGrid.getColumns().forEach(column -> ((Grid.Column)column).setResizable(true));
        support.setLabel("Support");
        support.setWidth("min-content");
        support.setMin(0);
        support.setValue(10);
        confidence.setLabel("Confidence (in %)");
        confidence.setValue(25.0);
        confidence.setWidth("min-content");
        confidence.setMin(0);
        confidence.setMax(100);
        support.setEnabled(false);
        confidence.setEnabled(false);
        buttonPrimary.setText("Generate");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(comboBoxGraph);
        layoutRow.add(comboBoxVersion);
        getContent().add(radioGroupQseType);
        getContent().add(graphInfo);
        getContent().add(searchField);
        getContent().add(classesGrid);
        getContent().add(checkbox);
        getContent().add(support);
        getContent().add(confidence);
        getContent().add(buttonPrimary);
        comboBoxGraph.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Long selectedValue = event.getValue().id;
                currentGraph = graphService.get(event.getValue().id).get();
                Utils.setComboBoxVersionsData(selectedValue, versionService, comboBoxVersion, true);
            }
        });
        comboBoxVersion.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                currentVersionId = event.getValue().id;
                setGridSampleData();
                currentVersion = versionService.get(event.getValue().id).get();
                Main.setDataSetNameForJar(currentGraph.getName() + "-" + event.getValue().label.replace(" ",""));
            }
        });
        checkbox.addValueChangeListener(event -> {
            if(event.getValue().booleanValue()) {
                support.setEnabled(false);
                confidence.setEnabled(false);
            }
            else
            {
                support.setEnabled(true);
                confidence.setEnabled(true);
            }
        });
        buttonPrimary.addClickListener(buttonClickEvent -> {
            try {
                completeFileBasedShapesExtraction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            getUI().ifPresent(ui -> ui.navigate(ShapesView.class, currentVersionId));
        });

        addAttachListener(event -> {
            setPaths();
        });
    }


    private void setGridSampleData() {
        //Copied from Shactor
        Version version = versionService.get(currentVersionId).get();
        parser = new Parser(version.getPath(), 50, 5000, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
        parser.entityExtraction();
        graphInfo.setVisible(true);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String info = "No. of entities: " + formatter.format(parser.entityDataHashMap.size()) + " ; " + "No. of classes: " + formatter.format(parser.classEntityCount.size()) + ". Please select the classes from the table below for which you want to extract shapes.";
        graphInfo.setText(info);
        setupGridInMultiSelectionMode(getClasses(parser.classEntityCount, parser.getStringEncoder()), parser.getStringEncoder(), parser.classEntityCount.size());
    }

    private void completeFileBasedShapesExtraction() throws IOException {
        parser.entityConstraintsExtraction();
        parser.computeSupportConfidence();

        //default shapes must be computed first
        System.out.println(chosenClasses);
        String outputAddress = parser.extractSHACLShapes(chosenClasses.size()>0, chosenClasses);
        List<NS> nodeShapes = parser.shapesExtractor.getNodeShapes();

        if(!checkbox.getValue().booleanValue()) {
            int supportValue = support.getValue();
            double confidenceValue = confidence.getValue()/100;

            outputAddress = parser.extractSHACLShapesWithPruning(!chosenClasses.isEmpty(), confidenceValue, supportValue, chosenClasses); // extract shapes with pruning
            System.out.println(prunedFileAddress);
            nodeShapes = parser.shapesExtractor.getNodeShapes();
            assert nodeShapes != null;
            PruningUtil pruningUtil = new PruningUtil();
            pruningUtil.applyPruningFlags(nodeShapes, supportValue, confidenceValue);
            pruningUtil.getDefaultStats(nodeShapes);
            pruningUtil.getStatsBySupport(nodeShapes);
            pruningUtil.getStatsByConfidence(nodeShapes);
            pruningUtil.getStatsByBoth(nodeShapes);
        }

        ExtractedShapes extractedShapes = new ExtractedShapes();
        extractedShapes.setVersionEntity(currentVersion);
        var classes = new ArrayList<String>();
        for (var i: classesGrid.getSelectedItems()) {
            classes.add(((Type)i).className);
        }
        extractedShapes.setClasses(classes);
        extractedShapes.setConfidence(!confidence.isEmpty() ? confidence.getValue() : 0.0);
        extractedShapes.setSupport(!support.isEmpty() ? support.getValue() : 0);
        extractedShapes.setQseType(QseType.valueOf(radioGroupQseType.getValue().toString()));
        extractedShapes.setCreatedAt(LocalDateTime.now());
        extractedShapes.setFileContent(Files.readAllBytes(Paths.get(outputAddress)));
        extractedShapes.setNodeShapes(nodeShapes);
        shapeService.insert(extractedShapes);
    }

    private static List<Type> getClasses(Map<Integer, Integer> classEntityCountMap, StringEncoder stringEncoder) {
        List<Type> types = new ArrayList<>();
        classEntityCountMap.forEach((k, v) -> {
            Type t = new Type();
            t.setName(stringEncoder.decode(k));
            t.setEncodedKey(v);
            t.setInstanceCount(v);
            types.add(t);
        });
        types.sort((d1, d2) -> d2.getInstanceCount() - d1.getInstanceCount());
        return types;
    }

    private void setupGridInMultiSelectionMode(List<Type> classes, StringEncoder encoder, Integer classEntityCountSize) {
        var dataView = classesGrid.setItems(classes);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(type -> {
            String searchTerm = searchField.getValue().trim();
            if (searchTerm.isEmpty())
                return true;
            Type t = (Type)type;
            return t.getName().toLowerCase().contains(searchTerm.toLowerCase());
        });

        classesGrid.addSelectionListener(selection -> {
            //System.out.printf("Number of selected classes: %s%n", selection.getAllSelectedItems().size());
            if (selection.getAllSelectedItems().size() == classEntityCountSize) {
                System.out.println("Extract Shapes for All Classes");
                chosenClasses = new ArrayList<>();
                chosenClassesEncoded = new HashSet<>();
            } else {
                System.out.println("Extract Shapes for Chosen Classes");
                chosenClasses = new ArrayList<>();
                chosenClassesEncoded = new HashSet<>();
                selection.getAllSelectedItems().forEach(item -> {
                    Type t = (Type) item;
                    chosenClasses.add(t.getName());
                    chosenClassesEncoded.add(encoder.encode(t.getName()));
                });
            }
            buttonPrimary.setEnabled(selection.getAllSelectedItems().size() > 0);
        });
        searchField.setVisible(true);

        for (var item: classes) {
            classesGrid.select(item);
        }
    }

    private static void setPaths() {
        try {
            String jarDir = System.getProperty("user.dir");
            Main.setOutputFilePathForJar(jarDir + "/Output/");
            Main.setConfigDirPathForJar(jarDir + "/config/");
            Main.setResourcesPathForJar(jarDir + "/resources/");
            Main.qseFromSpecificClasses = false;
            //Clean output directory
            File[] filesInOutputDir = new File(jarDir + "/Output/").listFiles();
            assert filesInOutputDir != null;
            for (File file : filesInOutputDir) {
                if (!file.getName().equals(".keep")) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("Deleted already existing file: " + file.getPath());
                    }
                }
                if (file.isDirectory()) {
                    FileUtils.forceDelete(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {
        currentVersionId = aLong;
        currentVersion = versionService.get(currentVersionId).get();
        currentGraph = currentVersion.getGraph();
        Utils.setComboBoxGraphData(graphService, comboBoxGraph);
        if(currentGraph != null)  {
            var graphItem = comboBoxGraph.getDataProvider().fetch(new Query<>()).filter(g -> g.id.equals(currentGraph.getId())).findFirst();
            comboBoxGraph.setValue(graphItem.get());
        }
        if(currentVersion != null) {
            var versionItem =  comboBoxVersion.getDataProvider().fetch(new Query<>())
                    .filter(v -> v.id.equals(aLong)).findFirst();
            comboBoxVersion.setValue(versionItem.get());
        }
    }
}
