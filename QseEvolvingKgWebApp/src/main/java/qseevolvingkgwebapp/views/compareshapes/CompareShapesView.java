package qseevolvingkgwebapp.views.compareshapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.views.MainLayout;
import qseevolvingkgwebapp.views.comparisondetails.ComparisonDetailsView;

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
        treeViewComparision.setHeight("70vh");
        getContent().add(layoutRow);
        layoutRow.add(multiSelectShapes);
        getContent().add(treeViewComparision);
        multiSelectShapes.addValueChangeListener(e -> {
            var extractedShapes = new ArrayList<ExtractedShapes>();
            for(var i : multiSelectShapes.getSelectedItems()) {
                extractedShapes.add(shapeService.get(i.id).get());
            }
            if(extractedShapes.stream().map(o -> o.getSupport()).distinct().count() > 1)
                Notification.show("Caution, the compared items do not have the same support-value!");
            if(extractedShapes.stream().map(o -> o.getConfidence()).distinct().count() > 1)
                Notification.show("Caution, the compared items do not have the same confidence-value!");
            if(extractedShapes.stream().map(o -> o.getClassesAsString()).distinct().count() > 1)
                Notification.show("Caution, the compared items were not analyzed for the same classes!");

            setTreeViewData();

        });
        treeViewComparision.addItemClickListener(event -> {
            ComparisionTreeViewItem clickedItem = event.getItem();
            VaadinSession.getCurrent().setAttribute("currentCompareObject", clickedItem);
            VaadinSession.getCurrent().setAttribute("currentComboBoxItems", multiSelectShapes.getSelectedItems());
            getUI().ifPresent(ui -> ui.navigate(ComparisonDetailsView.class));

        });
        addAttachListener(e ->{
            fillComboBox();
        });
    }

    private void fillComboBox() {
        var shapes = shapeService.listAll();
        var comboBoxItems = new ArrayList<Utils.ComboBoxItem>();

        for (var shape : shapes) {
            var comboBoxItem = new Utils.ComboBoxItem();
            comboBoxItem.id = shape.getId();
            comboBoxItem.label = Utils.getComboBoxLabelForExtractedShapes(shape);
            comboBoxItems.add(comboBoxItem);
        }
        multiSelectShapes.setItems(comboBoxItems);
        multiSelectShapes.setItemLabelGenerator(item -> item.label);
        if (comboBoxItems.size() > 1) {
            multiSelectShapes.setValue(comboBoxItems.get(0), comboBoxItems.get(1));
        }
    }

    private void setTreeViewData() {
        var nodeShapesToShow = new ArrayList<ComparisionTreeViewItem>();
        treeViewComparision.removeAllColumns();
        boolean first = true;
        for(var comboBoxItem : multiSelectShapes.getSelectedItems()) {
            var extractedShapes = shapeService.get(comboBoxItem.id).get();
            var nodeShapes = extractedShapes.getNodeShapes();
            var nodeShapesToShowMap =  nodeShapesToShow
                    .stream().map(ns -> ns.getShapeName())
                    .collect(Collectors.toList());
            for(var ns : nodeShapes) {
                if(nodeShapesToShowMap.contains(ns.getIri().getLocalName())) {
                    var nodeShapeToShow = nodeShapesToShow.stream().filter(n -> n.getShapeName()
                            .equals(ns.getIri().getLocalName())).findFirst().get();
                    nodeShapeToShow.addNodeShape(ns, comboBoxItem.id);
                }
                else {
                    var newItem = new ComparisionTreeViewItem();
                    newItem.addNodeShape(ns, comboBoxItem.id);
                    nodeShapesToShow.add(newItem);
                }
            }
            if(first) {
                treeViewComparision.addHierarchyColumn(o -> getTreeViewTextFromViewItem(o, comboBoxItem.id))
                    .setHeader(comboBoxItem.label);
                first = false;
            }
            else {
                treeViewComparision.addColumn(o -> getTreeViewTextFromViewItem(o, comboBoxItem.id)).setHeader(comboBoxItem.label);
            }
        }
        long startTime = System.nanoTime();

        treeViewComparision.setItems(nodeShapesToShow, this::getPropertyShapes);
        System.out.println("Method execution time: " + (System.nanoTime()-startTime)/ 1_000_000_000.0 + " seconds");

        treeViewComparision.expand(nodeShapesToShow);
    }

    private String getTreeViewTextFromViewItem(ComparisionTreeViewItem o, Long extractedShapesId) {
        if(o.isNodeShapeLine()) {
            if(o.getNodeShapeList().keySet().contains(extractedShapesId))
                return o.getShapeName();
            else
                return "-";
        }
        else {
            if(o.getPropertyShapeList().keySet().contains(extractedShapesId))
                return o.getShapeName();
            else
                return "-";
        }
    }

    private List<ComparisionTreeViewItem> getPropertyShapes(ComparisionTreeViewItem item) {
        var propertyShapesToShow = new ArrayList<ComparisionTreeViewItem>();
        if(item.getPropertyShapeList().size() == 0) { //important for performance
            for (var comboBoxItem : multiSelectShapes.getSelectedItems()) {
                var extractedShapes = shapeService.get(comboBoxItem.id).get().getNodeShapes()
                        .stream().filter(n -> n.getIri().getLocalName().equals(item.getShapeName())).findFirst();
                if (extractedShapes.isPresent()) {
                    var propertyShapesToShowMap = propertyShapesToShow
                            .stream().map(ns -> ns.getShapeName())
                            .collect(Collectors.toList());
                    for (var ps : extractedShapes.get().getPropertyShapeList()) {
                        if (propertyShapesToShowMap.contains(ps.getIri().getLocalName())) {
                            var propertyShapeToShow = propertyShapesToShow.stream().filter(n -> n.getShapeName()
                                    .equals(ps.getIri().getLocalName())).findFirst().get();
                            propertyShapeToShow.addPropertyShape(ps, comboBoxItem.id);
                        } else {
                            var newItem = new ComparisionTreeViewItem();
                            newItem.addPropertyShape(ps, comboBoxItem.id);
                            propertyShapesToShow.add(newItem);
                        }
                    }
                }

            }
        }
        return propertyShapesToShow;
    }
}
