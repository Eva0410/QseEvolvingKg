package qseevolvingkgwebapp.views.comparisiondetails;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import qseevolvingkgwebapp.services.ComparisionTreeViewItem;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.views.MainLayout;

@PageTitle("Comparision Details")
@Route(value = "comparision-details", layout = MainLayout.class)
@Uses(Icon.class)
public class ComparisionDetailsView extends Composite<VerticalLayout> {

    public ComparisionDetailsView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        H1 h1 = new H1();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        h1.setText("Shape - propertyShape");
        h1.setWidth("max-content");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(h1);
        getContent().add(layoutColumn2);
        addAttachListener(e -> {
            setUpView();
        });
    }
    private void setUpView() {
        var treeViewItem = (ComparisionTreeViewItem)VaadinSession.getCurrent().getAttribute("currentCompareObject");
        if(treeViewItem.isNodeShapeLine()) {

        }
        else {
            var firstItem = treeViewItem.getPropertyShapeList().get(treeViewItem.getPropertyShapeList().keySet().stream().findFirst().get());
            var nodeShape = firstItem.getNodeShape();
            var extractedShapes = nodeShape.getExtractedShapes();
            var model = extractedShapes.getModel();
            var output = Utils.generateTTLFromIRIInModel(firstItem.getIri(),model);
            System.out.println(output);
        }
        System.out.println();
    }
}
