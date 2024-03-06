package qseevolvingkgwebapp.views.compareshapes;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.NodeShape;
import qseevolvingkgwebapp.data.PropertyShape;
import qseevolvingkgwebapp.services.*;
import qseevolvingkgwebapp.views.MainLayout;
import qseevolvingkgwebapp.views.comparisondetails.ComparisonDetailsView;

@PageTitle("Compare Shapes")
@Route(value = "compare-shapes", layout = MainLayout.class)
@Uses(Icon.class)
@CssImport(
        themeFor = "vaadin-grid",
        value = "themes/qseevolvingkgwebapp/components/treeGridCustomCellBackground.css"
)
public class CompareShapesView extends Composite<VerticalLayout> {

    @Autowired()
    private VersionService versionService;
    @Autowired()
    private GraphService graphService;
    @Autowired()
    private ShapesService shapeService;
    MultiSelectComboBox<Utils.ComboBoxItem> multiSelectShapes;
    TreeGrid<ComparisionTreeViewItem> treeViewComparision;
    TextField filterField = new TextField("Filter");
    RadioButtonGroup<String> radioGroupFilter = new RadioButtonGroup<>();
    public CompareShapesView() {
        HorizontalLayout layoutRowComboBox = new HorizontalLayout();
        HorizontalLayout layoutRowFilter = new HorizontalLayout();
        multiSelectShapes = new MultiSelectComboBox();
        treeViewComparision = new TreeGrid<>();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setSpacing(false);
        layoutRowComboBox.addClassName(Gap.MEDIUM);
        layoutRowComboBox.setWidth("100%");
        layoutRowComboBox.setHeight("min-content");
        layoutRowFilter.addClassName(Gap.MEDIUM);
        layoutRowFilter.setWidth("100%");
        layoutRowFilter.setHeight("min-content");
        filterField.setHeight("min-content");
        multiSelectShapes.setLabel("Shapes");
        multiSelectShapes.setWidthFull();
        treeViewComparision.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS);
        treeViewComparision.setWidth("100%");
        treeViewComparision.getStyle().set("flex-grow", "0");
        treeViewComparision.setHeight("70vh");
        radioGroupFilter.setItems(Arrays.stream(FilterEnum.values()).map(FilterEnum::getLabel).collect(Collectors.toList()));
        radioGroupFilter.setValue(FilterEnum.ALL.getLabel());
        layoutRowFilter.setAlignItems(FlexComponent.Alignment.BASELINE);
        getContent().add(layoutRowComboBox);
        layoutRowComboBox.add(multiSelectShapes);
        getContent().add(layoutRowFilter);
        layoutRowFilter.add(filterField);
        layoutRowFilter.add(radioGroupFilter);
        getContent().add(treeViewComparision);

        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(event -> {
            var dataProvider = ((TreeDataProvider<ComparisionTreeViewItem>)treeViewComparision.getDataProvider());
            if (event.getValue() == null) {
                dataProvider.setFilter(null);
            } else {
                dataProvider.setFilter(item -> item.getShapeName().toLowerCase()
                        .contains(event.getValue().toLowerCase()));
            }
//            applyFilters();

        });

        radioGroupFilter.addValueChangeListener(event -> {
//            var dataProvider = ((TreeDataProvider<ComparisionTreeViewItem>)treeViewComparision.getDataProvider());
//            if (event.getValue().equals(FilterEnum.ALL.getLabel())) {
//                dataProvider.setFilter(null);
//            } else if (event.getValue().equals(FilterEnum.IDENTICAL.getLabel())) {
//                dataProvider.setFilter(item -> item.areShapesEqual());
//            } else {
//                dataProvider.setFilter(item -> !item.areShapesEqual());
//            }
//            applyFilters();

        });

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
            VaadinSession.getCurrent().setAttribute("currentCompareObject", event.getItem());
            VaadinSession.getCurrent().setAttribute("currentComboBoxItems", multiSelectShapes.getSelectedItems());
            getUI().ifPresent(ui -> ui.navigate(ComparisonDetailsView.class));
        });

        addAttachListener(e ->{
            fillComboBox();
        });
    }

//    private boolean filter(ComparisionTreeViewItem item, String filterText, String radioGroupText) {
//        if (radioGroupText.equals(FilterEnum.ALL.getLabel())) {
//            return item.getShapeName().toLowerCase()
//                    .contains(filterText.toLowerCase());
//        } else if (radioGroupText.equals(FilterEnum.IDENTICAL.getLabel())) {
//            dataProvider.setFilter(item -> item.areShapesEqual());
//        } else {
//            dataProvider.setFilter(item -> !item.areShapesEqual());
//        }
//    }

//    private void applyFilters() {
//        var dataProvider = (TreeDataProvider<ComparisionTreeViewItem>) treeViewComparision.getDataProvider();
//
//        // Get values from filter components
//        String filterValue = filterField.getValue();
//        String selectedFilter = radioGroupFilter.getValue();
//
//        // Define filter predicate based on filter values
//        if (filterValue == null && selectedFilter == null) {
//            dataProvider.setFilter(null); // No filter
//        } else {
//            dataProvider.setFilter(item -> {
//                // Apply filter based on filter field value
//                boolean filterFieldPass = filterValue == null || item.getShapeName().toLowerCase().contains(filterValue.toLowerCase());
//
//                // Apply filter based on radio group selection
//                boolean radioGroupPass = selectedFilter == null || (selectedFilter == FilterEnum.IDENTICAL.getLabel() && item.areShapesEqual())
//                        || (selectedFilter == FilterEnum.DIFFERENT.getLabel() && !item.areShapesEqual());
//
//                return filterFieldPass && radioGroupPass;
//            });
//            treeViewComparision.expandRecursively(dataProvider.getTreeData().getRootItems(),
//                    99);
//        }

        // Expand tree
//        treeViewComparision.expandRecursively(dataProvider.getTreeData().getRootItems(), 99);
//    }
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
        long startTime = System.nanoTime();

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

        addEqualInformationNS(nodeShapesToShow);

        treeViewComparision.setItems(nodeShapesToShow, this::getPropertyShapes);
        System.out.println("Method execution time: " + (System.nanoTime()-startTime)/ 1_000_000_000.0 + " seconds");

        treeViewComparision.expand(nodeShapesToShow);
        treeViewComparision.setClassNameGenerator(e -> !e.areShapesEqual() ? "warn" : null);
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
        addEqualInformationPS(propertyShapesToShow);
        return propertyShapesToShow;
    }

    private void addEqualInformationPS(ArrayList<ComparisionTreeViewItem> propertyShapesToShow) {
        for (var comparisonTreeViewItem:
             propertyShapesToShow) {
            for (var ps : comparisonTreeViewItem.getPropertyShapeList().values()) {
                if(ps.getGeneratedText() == null || ps.getGeneratedText().isEmpty()) {
                    ps.generateText();
                    shapeService.update(ps.getNodeShape().getExtractedShapes());
                }

            }
            comparisonTreeViewItem.setShapesEqual(
                    areAllStringsEqual(
                            comparisonTreeViewItem.getPropertyShapeList().values().stream()
                                    .map(PropertyShape::getGeneratedText).collect(Collectors.toList())));

        }
    }

    private void addEqualInformationNS(ArrayList<ComparisionTreeViewItem> propertyShapesToShow) {
        for (var comparisonTreeViewItem:
                propertyShapesToShow) {
            for (var ps : comparisonTreeViewItem.getNodeShapeList().values()) {
                if(ps.getGeneratedText() == null || ps.getGeneratedText().isEmpty()) {
                    ps.generateText();
                    shapeService.update(ps.getExtractedShapes());
                }
            }
            comparisonTreeViewItem.setShapesEqual(
                    areAllStringsEqual(
                            comparisonTreeViewItem.getNodeShapeList().values().stream()
                                    .map(NodeShape::getGeneratedText).collect(Collectors.toList())));

        }
    }
    private Boolean areAllStringsEqual(List<String> texts) {
        if(texts.size() != multiSelectShapes.getSelectedItems().size())
            return false;
        for (int i = 0; i < texts.size(); i++) {
            for (int j = i + 1; j < texts.size(); j++) {
                if (!texts.get(i).equals(texts.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public enum FilterEnum {
        ALL("All"),
        IDENTICAL("Identical shapes"),
        DIFFERENT("Different shapes");

        private final String label;

        private FilterEnum(String label) {
            this.label = label;
        }public String getLabel() {
            return label;
        }
    }
}
