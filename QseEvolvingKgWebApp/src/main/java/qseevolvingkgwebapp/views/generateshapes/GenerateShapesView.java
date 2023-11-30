package qseevolvingkgwebapp.views.generateshapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.QseType;
import qseevolvingkgwebapp.data.SamplePerson;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.services.Utils.ComboBoxItem;
import qseevolvingkgwebapp.views.MainLayout;

@PageTitle("Generate Shapes")
@Route(value = "generate-shapes", layout = MainLayout.class)
@Uses(Icon.class)
public class GenerateShapesView extends Composite<VerticalLayout> {

    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;

    @Autowired()
    private ShapesService shapeService;

    Select<ComboBoxItem> comboBoxGraph;
    Select<ComboBoxItem> comboBoxVersion;

    public GenerateShapesView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow.setSpacing(true);
        layoutRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        comboBoxGraph = new Select<>();
        comboBoxVersion = new Select<>();
        RadioButtonGroup radioGroup = new RadioButtonGroup();
        Grid multiSelectGrid = new Grid(SamplePerson.class);
        TextField textField = new TextField();
        TextField textField2 = new TextField();
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        comboBoxGraph.setLabel("Graph");
        comboBoxGraph.setWidth("min-content");
        comboBoxVersion.setLabel("Version");
        comboBoxVersion.setWidth("min-content");
        radioGroup.setLabel("QSE-Type");
        radioGroup.setWidth("min-content");
        radioGroup.setItems(Arrays.stream(QseType.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        multiSelectGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        multiSelectGrid.setWidth("100%");
        multiSelectGrid.getStyle().set("flex-grow", "0");
        setGridSampleData(multiSelectGrid);
        textField.setLabel("Support");
        textField.setWidth("min-content");
        textField2.setLabel("Confidence");
        textField2.setWidth("min-content");
        buttonPrimary.setText("Generate");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(comboBoxGraph);
        layoutRow.add(comboBoxVersion);
        getContent().add(radioGroup);
        getContent().add(multiSelectGrid);
        getContent().add(textField);
        getContent().add(textField2);
        getContent().add(buttonPrimary);
        comboBoxGraph.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Long selectedValue = event.getValue().id;
                setComboBoxVersionsData(selectedValue);
            }
        });
        addAttachListener(event -> {
            setComboBoxGraphData();
        });
    }

    private void setComboBoxGraphData() {
        List<Utils.ComboBoxItem> graphs = Utils.getAllGraphs(graphService);
        comboBoxGraph.setItems(graphs);
        comboBoxGraph.setItemLabelGenerator(item -> item.label);
        if (graphs.size() > 0) {
            var firstItem = graphs.stream().findFirst();
            comboBoxGraph.setValue(firstItem.get());
        }
    }

    private void setComboBoxVersionsData(Long graphId) {
        List<Utils.ComboBoxItem> versions = Utils.getAllVersions(versionService, graphId);
        comboBoxVersion.setItems(versions);
        comboBoxVersion.setItemLabelGenerator(item -> item.label);
        if (versions.size() > 0) {
            var firstItem = versions.stream().findFirst();
            comboBoxVersion.setValue(firstItem.get());
        }
    }

    private void setGridSampleData(Grid grid) {
        //Get classes
    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
