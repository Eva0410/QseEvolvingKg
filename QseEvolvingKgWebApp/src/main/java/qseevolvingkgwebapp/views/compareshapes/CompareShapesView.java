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
    TreeGrid<ComparisonTreeViewItem> treeViewComparision;
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
        var currentSearchFilter = (String)VaadinSession.getCurrent().getAttribute("comparison_searchValue");
        if(currentSearchFilter != null)
            filterField.setValue(currentSearchFilter);
        var currentFilter = (String)VaadinSession.getCurrent().getAttribute("comparison_filterValue");
        if(currentFilter != null)
            radioGroupFilter.setValue(currentFilter);
        layoutRowFilter.setAlignItems(FlexComponent.Alignment.BASELINE);
        getContent().add(layoutRowComboBox);
        layoutRowComboBox.add(multiSelectShapes);
        getContent().add(layoutRowFilter);
        layoutRowFilter.add(filterField);
        layoutRowFilter.add(radioGroupFilter);
        getContent().add(treeViewComparision);

        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(event -> {
            applyFilters();
        });

        radioGroupFilter.addValueChangeListener(event -> {
            applyFilters();
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
            VaadinSession.getCurrent().setAttribute("currentComboBoxItems", multiSelectShapes.getSelectedItems());
        });
        treeViewComparision.addItemClickListener(event -> {
            VaadinSession.getCurrent().setAttribute("currentCompareObject", event.getItem());
            getUI().ifPresent(ui -> ui.navigate(ComparisonDetailsView.class));
        });

        addAttachListener(e -> {
            fillComboBox();
            applyFilters();
        });
    }

    private void applyFilters() {
        var dataProvider = (TreeDataProvider<ComparisonTreeViewItem>) treeViewComparision.getDataProvider();

        // Get values from filter components
        String searchValue = filterField.getValue();
        String selectedRadioGroupFilter = radioGroupFilter.getValue();
        VaadinSession.getCurrent().setAttribute("comparison_searchValue", searchValue);
        VaadinSession.getCurrent().setAttribute("comparison_filterValue", selectedRadioGroupFilter);

        // Define filter predicate based on filter values
        if (searchValue == null && selectedRadioGroupFilter == null) {
            dataProvider.setFilter(null);
        } else {
            dataProvider.setFilter(item -> {
                boolean filterFieldPass = searchValue == null || item.getShapeName().toLowerCase().contains(searchValue.toLowerCase());

                boolean radioGroupPass = selectedRadioGroupFilter == null ||
                        (selectedRadioGroupFilter == FilterEnum.IDENTICALPS.getLabel() && item.areShapesEqual()) ||
                        (selectedRadioGroupFilter == FilterEnum.IDENTICALNS.getLabel() && item.areShapesEqual() && filterParentNodeShape(item)) ||
                        (selectedRadioGroupFilter == FilterEnum.DIFFERENT.getLabel() && !item.areShapesEqual()) ||
                        (selectedRadioGroupFilter == FilterEnum.ALL.getLabel());

                return filterFieldPass && radioGroupPass;
            });
            treeViewComparision.expandRecursively(dataProvider.getTreeData().getRootItems(),
                    99);
        }
        treeViewComparision.expandRecursively(dataProvider.getTreeData().getRootItems(), 99);
    }

    private Boolean filterParentNodeShape(ComparisonTreeViewItem child) {
        if(!child.isNodeShapeLine() && child != null) {
            var parent = child.getParentShape();
            return parent != null && parent.areShapesEqual();
        }
        return true;
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

        var currentComboBoxItems = (Set<Utils.ComboBoxItem>)VaadinSession.getCurrent().getAttribute("currentComboBoxItems");
        if(currentComboBoxItems != null && currentComboBoxItems.size() > 0
                && currentComboBoxItems.stream()
                .map(item -> item.id)
                .allMatch(id -> comboBoxItems.stream()
                        .map(comboBoxItem -> comboBoxItem.id)
                        .collect(Collectors.toSet())
                        .contains(id))) {
            for (var cbi :
                    currentComboBoxItems) {
                var newComboBoxItem = comboBoxItems.stream().filter(c -> c.id.equals(cbi.id)).findFirst();
                if(newComboBoxItem.isPresent())
                    multiSelectShapes.select(newComboBoxItem.get());
            }
        }else {
            if (comboBoxItems.size() > 1) {
                multiSelectShapes.setValue(comboBoxItems.get(0), comboBoxItems.get(1));
            }
        }
    }

    private void setTreeViewData() {
        var nodeShapesToShow = new ArrayList<ComparisonTreeViewItem>();
        treeViewComparision.removeAllColumns();
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
                    var newItem = new ComparisonTreeViewItem();
                    newItem.addNodeShape(ns, comboBoxItem.id);
                    nodeShapesToShow.add(newItem);
                }
            }
            treeViewComparision.addHierarchyColumn(o -> getTreeViewTextFromViewItem(o, comboBoxItem.id))
                .setHeader(comboBoxItem.label);

        }

        addEqualInformationNS(nodeShapesToShow);

        treeViewComparision.setItems(nodeShapesToShow, this::getPropertyShapes);

        treeViewComparision.expand(nodeShapesToShow);
        treeViewComparision.setClassNameGenerator(e -> !e.areShapesEqual() ? "warn" : null);
    }

    private String getTreeViewTextFromViewItem(ComparisonTreeViewItem o, Long extractedShapesId) {
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

    private List<ComparisonTreeViewItem> getPropertyShapes(ComparisonTreeViewItem item) {
        var propertyShapesToShow = new ArrayList<ComparisonTreeViewItem>();
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
                            var newItem = new ComparisonTreeViewItem();
                            newItem.addPropertyShape(ps, comboBoxItem.id);
                            newItem.setParentShape(item);
                            propertyShapesToShow.add(newItem);
                        }
                    }
                }

            }
        }
        addEqualInformationPS(propertyShapesToShow);
        return propertyShapesToShow;
    }

    private void addEqualInformationPS(ArrayList<ComparisonTreeViewItem> propertyShapesToShow) {
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

    private void addEqualInformationNS(ArrayList<ComparisonTreeViewItem> propertyShapesToShow) {
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
        IDENTICALNS("Identical node shapes"),
        IDENTICALPS("Identical property shapes"),
        DIFFERENT("Different shapes");

        private final String label;

        FilterEnum(String label) {
            this.label = label;
        }public String getLabel() {
            return label;
        }
    }
}
