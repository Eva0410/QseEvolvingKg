package qseevolvingkgwebapp.views.comparisondetails;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.ExtractedShapes;
import qseevolvingkgwebapp.data.NodeShape;
import qseevolvingkgwebapp.data.PropertyShape;
import qseevolvingkgwebapp.services.ComparisonTreeViewItem;
import qseevolvingkgwebapp.services.ShapesService;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.views.MainLayout;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "comparison-details", layout = MainLayout.class)
@Uses(Icon.class)
public class ComparisonDetailsView extends Composite<VerticalLayout> implements HasDynamicTitle {
    private ComparisonDiv comparisonDiv;
    private Select<Utils.ComboBoxItem> selectItemOld;
    private Select<Utils.ComboBoxItem> selectItemNew;
    private String oldText;
    private String newText;
    private ComparisonTreeViewItem treeViewItem;

    @Autowired
    ShapesService shapesService;

    public ComparisonDetailsView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        getContent().add(layout);
        layout.add(layoutRow);
        selectItemOld = new Select<>();
        selectItemOld.setLabel("First version to compare");
        selectItemOld.setWidth("100%");
        selectItemNew = new Select<>();
        selectItemNew.setLabel("Second version to compare");
        selectItemNew.setWidth("100%");
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("justify-content", "space-between");
        layoutRow.add(selectItemOld);
        layoutRow.add(selectItemNew);
        treeViewItem = (ComparisonTreeViewItem)VaadinSession.getCurrent().getAttribute("currentCompareObject");

        var comboBoxItems = (Set<Utils.ComboBoxItem>)VaadinSession.getCurrent().getAttribute("currentComboBoxItems");
        if(comboBoxItems != null) {
            selectItemOld.setItems(comboBoxItems);
            selectItemOld.setItemLabelGenerator(item -> item.label);
            selectItemNew.setItems(comboBoxItems);
            selectItemNew.setItemLabelGenerator(item -> item.label);

            var list = selectItemOld.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
            selectItemOld.setValue(list.get(0));
            selectItemNew.setValue(list.get(list.size() - 1));
            if(treeViewItem != null) {
                oldText = Utils.escapeNew(getText(list.get(0).id));
                newText = Utils.escapeNew(getText(list.get(list.size() - 1).id));
            }
        }

        comparisonDiv = new ComparisonDiv(oldText, newText);
        layout.add(comparisonDiv);

        selectItemOld.addValueChangeListener(e -> {
            oldText = Utils.escapeNew(getText(e.getValue().id));
            comparisonDiv.updateTextDifferences(oldText,newText);
        });
        selectItemNew.addValueChangeListener(e -> {
            newText = Utils.escapeNew(getText(e.getValue().id));
            comparisonDiv.updateTextDifferences(oldText,newText);
        });

        getContent().addAttachListener(e -> {
            layout.add(new H2("All SHACL shapes"));
            for(var key : getKeySetFromTreeViewItem()) {
                layout.add(new H4(Utils.getComboBoxLabelForExtractedShapes(shapesService.get(key).get())));
                Div div = new Div();
                div.getElement().setProperty("innerHTML", convertNewlinesToHtmlBreaks(getText(key)));
                layout.add(div);
            }
        });
    }

    private List<Long> getKeySetFromTreeViewItem() {
        if(treeViewItem.isNodeShapeLine())
        {
            var nsComparator = Comparator
                    .comparing(ns -> ((NodeShape)ns).getExtractedShapes().getGraphCreationTime())
                    .thenComparing(ns -> ((NodeShape)ns).getExtractedShapes().getVersionCreationTime())
                    .thenComparing(ns -> ((NodeShape)ns).getExtractedShapes().getCreatedAt());

            return treeViewItem.getNodeShapeList().values().stream()
                    .sorted(nsComparator)
                    .map(ns -> ns.getExtractedShapes().getId()).collect(Collectors.toList());
        }
        else {
            var psComparator = Comparator
                    .comparing(ps -> ((PropertyShape)ps).getNodeShape().getExtractedShapes().getGraphCreationTime())
                    .thenComparing(ps -> ((PropertyShape)ps).getNodeShape().getExtractedShapes().getVersionCreationTime())
                    .thenComparing(ps -> ((PropertyShape)ps).getNodeShape().getExtractedShapes().getCreatedAt());

            return treeViewItem.getPropertyShapeList().values().stream()
                    .sorted(psComparator).collect(Collectors.toList())
                    .stream().map(ps -> ps.getNodeShape().getExtractedShapes().getId()).collect(Collectors.toList());
        }
    }

    private String getText(Long extractedShapesId) {
        if(treeViewItem.isNodeShapeLine()) {
            NodeShape ns = treeViewItem.getNodeShapeList().get(extractedShapesId);
            if(ns == null)
                return "";
            return ns.getGeneratedText();
        }
        else {
            var ps = treeViewItem.getPropertyShapeList().get(extractedShapesId);
            if(ps == null)
                return "";
            return ps.getGeneratedText();
        }
    }
    public static String convertNewlinesToHtmlBreaks(String input) {
        String s = ComparisonDiv.escapeHtmlCharacters(input);
        if(Utils.usePrettyFormatting)
            return s.replaceAll("\r","").replaceAll("\n", "<br>");
        else
            return s.replaceAll("\\\\\\\\n", "<br>");
    }

    @Override
    public String getPageTitle() {
        if(treeViewItem == null)
            return "Comparison Detail";
        return "Comparison Detail - " + treeViewItem.getShapeName();
    }
}
