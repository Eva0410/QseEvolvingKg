package qseevolvingkgwebapp.views.graphs;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.services.GraphService;
import qseevolvingkgwebapp.views.MainLayout;

@PageTitle("Graphs")
@Route(value = "graphs", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class GraphsView extends Composite<VerticalLayout> {
    @Autowired()
    private GraphService graphService;

    private Grid gridGraphs;

    public GraphsView() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button buttonNewGraph = new Button();
        gridGraphs = new Grid(Graph.class);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        horizontalLayout.addClassName(Gap.MEDIUM);
        horizontalLayout.setWidth("100%");
        horizontalLayout.setHeight("min-content");
        buttonNewGraph.setText("New Graph");
        buttonNewGraph.setWidth("min-content");
        buttonNewGraph.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonNewGraph.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate("new-graph"));
        });
        gridGraphs.setWidth("100%");
        gridGraphs.getStyle().set("flex-grow", "0");
        gridGraphs.getColumns().forEach(column -> ((Grid.Column)column).setResizable(true));
        gridGraphs.setColumns("name","createdAt");
        gridGraphs.addColumn(new ComponentRenderer<>(Button::new, (button, gr) -> {
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> {
                Graph graph = (Graph) gr;
                graphService.delete(graph.getId());
                setGraphsData();
            });
            button.setIcon(new Icon(VaadinIcon.TRASH));
        })).setHeader("");
        setGraphsData();
        getContent().add(horizontalLayout);
        horizontalLayout.add(buttonNewGraph);
        getContent().add(gridGraphs);
    }

    private void setGraphsData() {
        gridGraphs.setItems(query -> graphService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }
}
