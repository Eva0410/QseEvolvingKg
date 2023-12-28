package qseevolvingkgwebapp.views.versions;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.Utils.ComboBoxItem;
import qseevolvingkgwebapp.services.GraphService;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.services.VersionService;
import qseevolvingkgwebapp.views.MainLayout;
import qseevolvingkgwebapp.views.newversion.NewVersionView;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Versions")
@Route(value = "versions", layout = MainLayout.class)
@Uses(Icon.class)
public class VersionsView extends Composite<VerticalLayout>  {
    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;

    private Grid basicGrid;

    public VersionsView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        Select<ComboBoxItem> comboBox = new Select<>();
        Button buttonPrimary = new Button();
        basicGrid = new Grid(Version.class);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow.setSpacing(true);
        layoutRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        comboBox.setLabel("Graph");
        comboBox.setWidth("min-content");
        basicGrid.setWidth("100%");
        basicGrid.getStyle().set("flex-grow", "0");
        basicGrid.setColumns("versionNumber", "name", "createdAt","path");
        basicGrid.getColumns().forEach(column -> ((Grid.Column)column).setResizable(true));
        buttonPrimary.setText("New Version");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(NewVersionView.class,comboBox.getValue().id));
        });
        getContent().add(layoutRow);
        layoutRow.add(comboBox);
        layoutRow.add(buttonPrimary);
        getContent().add(basicGrid);
        comboBox.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Long selectedValue = event.getValue().id;
                fillGrid(basicGrid, selectedValue);
            }
        });

        addAttachListener(event -> {
            setComboBoxSampleData(comboBox, basicGrid);
        });
    }

    private void setComboBoxSampleData(Select<ComboBoxItem> comboBox, Grid grid) {
        List<ComboBoxItem> comboBoxItemList = graphService.listAll().stream()
                .map(graph -> new ComboBoxItem(graph.getName(),graph.getId()))
                .collect(Collectors.toList());
        comboBox.setItems(comboBoxItemList);
        comboBox.setItemLabelGenerator(item -> ((ComboBoxItem)item).label);
        if (comboBoxItemList.size() > 0) {
            var firstItem = comboBoxItemList.stream().findFirst();
            comboBox.setValue(firstItem.get());
        }
    }

    private void fillGrid(Grid grid, Long graphId) {
        grid.setItems(query -> versionService.listByGraphId(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)), graphId)
                .stream());
    }
}

