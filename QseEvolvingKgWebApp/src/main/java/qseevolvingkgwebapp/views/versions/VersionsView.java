package qseevolvingkgwebapp.views.versions;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.Utils.ComboBoxItem;
import qseevolvingkgwebapp.services.GraphService;
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

    private Grid gridVersions;
    private Long currentGraphId;

    public VersionsView() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Select<ComboBoxItem> comboBoxGraphs = new Select<>();
        Button buttonNewVersion = new Button();
        gridVersions = new Grid(Version.class);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        horizontalLayout.setWidth("100%");
        horizontalLayout.setHeight("min-content");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        comboBoxGraphs.setLabel("Graph");
        comboBoxGraphs.setWidth("min-content");
        gridVersions.setWidth("100%");
        gridVersions.getStyle().set("flex-grow", "0");
        gridVersions.setColumns("versionNumber", "name", "createdAt","path");
        gridVersions.addColumn(new ComponentRenderer<>(Button::new, (button, ver) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> {
                Version version = (Version) ver;
                versionService.delete(version.getId());
                fillGrid();
            });
            button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("");
        gridVersions.getColumns().forEach(column -> ((Grid.Column)column).setResizable(true));
        buttonNewVersion.setText("New Version");
        buttonNewVersion.setWidth("min-content");
        buttonNewVersion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonNewVersion.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(NewVersionView.class,comboBoxGraphs.getValue().id));
        });
        getContent().add(horizontalLayout);
        horizontalLayout.add(comboBoxGraphs);
        horizontalLayout.add(buttonNewVersion);
        getContent().add(gridVersions);
        comboBoxGraphs.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                currentGraphId = event.getValue().id;
                fillGrid();
            }
        });

        addAttachListener(event -> {
            setGraphs(comboBoxGraphs);
        });
    }

    private void setGraphs(Select<ComboBoxItem> comboBox) {
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

    private void fillGrid() {
        gridVersions.setItems(query -> versionService.listByGraphId(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)), currentGraphId)
                .stream());
    }
}

