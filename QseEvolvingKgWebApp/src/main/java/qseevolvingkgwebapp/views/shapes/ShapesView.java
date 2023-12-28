package qseevolvingkgwebapp.views.shapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cs.Main;
import cs.qse.filebased.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.data.SamplePerson;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.views.MainLayout;
import qseevolvingkgwebapp.views.generateshapes.GenerateShapesView;

@PageTitle("Shapes")
@Route(value = "shapes", layout = MainLayout.class)
@Uses(Icon.class)
public class ShapesView extends Composite<VerticalLayout> implements HasUrlParameter<Long> {

    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;
    @Autowired()
    private ShapesService shapeService;
    Select<Utils.ComboBoxItem> selectItemGraph;
    Select<Utils.ComboBoxItem> selectItemVersion;
    Grid gridShapes;
    Graph currentGraph;
    Version currentVersion;
    Long currentVersionId;
    public ShapesView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRowButton = new HorizontalLayout();
        selectItemGraph = new Select<Utils.ComboBoxItem>();
        selectItemVersion = new Select<Utils.ComboBoxItem>();
        Button buttonGenerateShapes = new Button();
        gridShapes = new Grid(ExtractedShapes.class, false);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRowButton.addClassName(Gap.MEDIUM);
        layoutRowButton.setWidth("100%");
        layoutRowButton.setHeight("min-content");
        selectItemGraph.setLabel("Graph");
        selectItemGraph.setWidth("min-content");
        selectItemVersion.setLabel("Version");
        selectItemVersion.setWidth("min-content");
        buttonGenerateShapes.setText("Generate Shapes");
        buttonGenerateShapes.setWidth("min-content");
        buttonGenerateShapes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        gridShapes.setWidth("100%");
        gridShapes.getStyle().set("flex-grow", "0");
        getContent().add(layoutRow);
        getContent().add(layoutRowButton);
        layoutRow.add(selectItemGraph);
        layoutRow.add(selectItemVersion);
        layoutRowButton.add(buttonGenerateShapes);
        getContent().add(gridShapes);

        selectItemGraph.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Long selectedValue = event.getValue().id;
                currentGraph = graphService.get(selectedValue).get();
                selectItemVersion =  Utils.setComboBoxVersionsData(selectedValue, versionService, selectItemVersion, false);
            }
        });

        selectItemVersion.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                currentVersionId = event.getValue().id;
                currentVersion = versionService.get(currentVersionId).get();
                setGridData();
            }
        });

        buttonGenerateShapes.addClickListener(buttonClickEvent -> getUI().ifPresent(ui -> ui.navigate(GenerateShapesView.class, currentVersionId)));
    }

    private void setGridData() {
        gridShapes.addColumn(o -> ((ExtractedShapes) o).getCreatedAt()).setHeader("Created At");
        gridShapes.addColumn(o -> ((ExtractedShapes) o).getQseType()).setHeader("QSE Type");
        gridShapes.addColumn(o -> ((ExtractedShapes) o).getSupport()).setHeader("Support");
        gridShapes.addColumn(o -> ((ExtractedShapes) o).getConfidence()).setHeader("Confidence");
        gridShapes.addColumn(o -> ((ExtractedShapes) o).getClassesAsString()).setHeader("Classes");
        var items = shapeService.listByVersionId(currentVersionId);

        if(items != null)
            gridShapes.setItems(items);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Long aLong) {
        Utils.setComboBoxGraphData(graphService, selectItemGraph);
        if(aLong == null || aLong == 0) {
            Utils.setComboBoxVersionsData(currentGraph.getId(), versionService, selectItemVersion, true);
        }
        else {
            currentVersionId = aLong;
            currentVersion = versionService.get(currentVersionId).get();
            currentGraph = currentVersion.getGraph();
            if(currentGraph != null)  {
                var graphItem = selectItemGraph.getDataProvider().fetch(new Query<>()).filter(g -> g.id.equals(currentGraph.getId())).findFirst();
                selectItemGraph.setValue(graphItem.get());
            }
            if(currentVersion != null) {
                var versionItem =  selectItemVersion.getDataProvider().fetch(new Query<>())
                        .filter(v -> v.id.equals(aLong)).findFirst();
                selectItemVersion.setValue(versionItem.get());
            }
        }
    }
}
