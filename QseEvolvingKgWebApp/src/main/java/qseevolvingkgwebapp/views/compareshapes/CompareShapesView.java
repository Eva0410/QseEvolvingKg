package qseevolvingkgwebapp.views.compareshapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cs.qse.common.structure.NS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import qseevolvingkgwebapp.data.NodeShape;
import qseevolvingkgwebapp.data.PropertyShape;
import qseevolvingkgwebapp.data.SamplePerson;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.views.MainLayout;

@PageTitle("Compare Shapes")
@Route(value = "compare-shapes", layout = MainLayout.class)
@Uses(Icon.class)
public class CompareShapesView extends Composite<VerticalLayout> {

    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;
    @Autowired()
    private ShapesService shapeService;
    MultiSelectComboBox<Utils.ComboBoxItem> multiSelectShapes;
    TreeGrid<ComparisionTreeViewItem> treeViewComparision;

    public CompareShapesView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        multiSelectShapes = new MultiSelectComboBox();
        treeViewComparision = new TreeGrid<>();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        multiSelectShapes.setLabel("Shapes");
        multiSelectShapes.setWidthFull();
        treeViewComparision.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS);
        treeViewComparision.setWidth("100%");
        treeViewComparision.getStyle().set("flex-grow", "0");
        setGridSampleData(treeViewComparision);
        getContent().add(layoutRow);
        layoutRow.add(multiSelectShapes);
        getContent().add(treeViewComparision);
        addAttachListener(e ->{
            fillComboBox();
            setMultiSelectComboBoxSampleData();
        });
    }

    private void fillComboBox() {
        var shapes = shapeService.listAll();
        var comboBoxItems = new ArrayList<Utils.ComboBoxItem>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (var shape : shapes) {
            var comboBoxItem = new Utils.ComboBoxItem();
            comboBoxItem.id = shape.getId();
            var version = shape.getVersionEntity();
            var graph = version.getGraph();
            comboBoxItem.label = graph.getName() + "-" + version.getVersionNumber() + "-" + version.getName() + "-"
                    + formatter.format(shape.getCreatedAt()) + "-"
                    + shape.getQseType() + "-" + shape.getSupport() + "-" + shape.getConfidence();
            comboBoxItems.add(comboBoxItem);
        }
        multiSelectShapes.setItems(comboBoxItems);
        multiSelectShapes.setItemLabelGenerator(item -> item.label);
        if (comboBoxItems.size() > 1) {
            multiSelectShapes.setValue(comboBoxItems.get(0), comboBoxItems.get(1));
        }
    }

    record SampleItem(String value, String label, Boolean disabled) {
    }

    private void setMultiSelectComboBoxSampleData() {
        var nodeShapesToShow = new ArrayList<ComparisionTreeViewItem>();
        treeViewComparision.removeAllColumns();
        treeViewComparision.addHierarchyColumn(ComparisionTreeViewItem::getShapeName)
                .setHeader("NodeShape");
        for(var comboBoxItem : multiSelectShapes.getSelectedItems()) {
            var extractedShapes = shapeService.get(comboBoxItem.id).get();
            var nodeShapes = extractedShapes.getNodeShapes();
            var selected = nodeShapes.stream().filter(n -> !nodeShapesToShow
                            .stream().map(ns -> ns.getShapeName())
                            .collect(Collectors.toList())
                        .contains(n.getIri().getLocalName()))
                    .map(ns -> new ComparisionTreeViewItem(ns))
                    .collect(Collectors.toList());
            nodeShapesToShow.addAll(selected);

            treeViewComparision.addColumn(o -> {
                return o.getShapeName();
            }).setHeader(comboBoxItem.label);
        }
        treeViewComparision.setItems(nodeShapesToShow, this::getPropertyShapes);
    }

    private List<ComparisionTreeViewItem> getPropertyShapes(ComparisionTreeViewItem item) {
        
        if(item.getShapeName()=="OntologyShape")
        {
            var propertyShapes = new ArrayList<ComparisionTreeViewItem>();
       propertyShapes.add(new ComparisionTreeViewItem("test"));
     return propertyShapes;        }

//        var propertyShapes = new ArrayList<ComparisionTreeViewItem>();
//
//        i.add(new ComparisionTreeViewItem(item.getShapeName()+"i"));
//        return i;
return new ArrayList<>();
    }

    private void setGridSampleData(Grid grid) {

    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
