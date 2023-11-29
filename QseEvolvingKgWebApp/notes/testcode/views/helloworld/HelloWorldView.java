package com.example.application.views.helloworld;

import com.example.application.views.DivExample;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.events.internal.DataAddedEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.io.BufferedReader;
import java.io.FileReader;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class HelloWorldView extends HorizontalLayout {

//    private TextField name;
//    private Button sayHello;

    public HelloWorldView() {
//        name = new TextField("Your name");
//        sayHello = new Button("Say hello");
//        sayHello.addClickListener(e -> {
//            Notification.show("Hello " + name.getValue());
//        });
//        sayHello.addClickShortcut(Key.ENTER);

        //setWidthFull(); // Set the layout to take the full width
//        String filePath = "/path/to/your/file.txt"; // Replace with your file path
//        StringBuilder content = new StringBuilder();
//
//        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\evapu\\Documents\\GitHub\\qse\\Output\\TEMP\\lubm-mini_QSE_0.1_100_SHACL.ttl"))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                content.append(line).append("\n");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //HorizontalLayout layout = new HorizontalLayout(); // Horizontal layout to hold the divs
        //layout.setWidth("100%");
        // Create two Div components for the boxes


//        VerticalLayout layout = new VerticalLayout();
//        layout.add(new Button("Button 1"));
//
//        layout.add(new Button("Button 2"));
//        layout.add(new Button("Button 3"));
//        add(layout);

        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        add(layout);
        layout.add(new H2("Old SHACL shapes"));

        Div box1 = new Div();
        String text1 = "<http://shaclshapes.org/doctoralDegreeFromLecturerShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://shaclshapes.org/confidence> 1E0 ;\n" +
                "  <http://shaclshapes.org/support> \"210\"^^xsd:int ;\n" +
                "  <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#IRI> ;\n" +
                "  <http://www.w3.org/ns/shacl#class> <http://swat.cse.lehigh.edu/onto/univ-bench.owl#University> ;\n" +
                "  <http://www.w3.org/ns/shacl#minCount> 1 ;\n" +
                "  <http://www.w3.org/ns/shacl#node> <http://shaclshapes.org/UniversityShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#path> <http://swat.cse.lehigh.edu/onto/univ-bench.owl#doctoralDegreeFrom> .";
        box1.getElement().setProperty("innerHTML", convertNewlinesToHtmlBreaks(text1));
        layout.add(box1);
        layout.add(new H2("New SHACL shapes"));

        Div box2 = new Div();
        String text2 = "<http://shaclshapes.org/doctoralDegreeFromLecturerShapeProperty> rdf:type <http://www.w3.org/ns/shacl#PropertyShape> ;\n" +
                "  <http://shaclshapes.org/confidence> 1E0 ;\n" +
                "  <http://shaclshapes.org/support> \"1230\"^^xsd:int ;\n" +
                "  <http://www.w3.org/ns/shacl#NodeKind> <http://www.w3.org/ns/shacl#IRI> ;\n" +
                "  <http://www.w3.org/ns/shacl#class> <http://swat.cse.lehigh.edu/onto/univ-bench.owl#University> ;\n" +
                "  <http://www.w3.org/ns/shacl#minCount>51 ;\n" +
                "  <http://www.w3.org/ns/shacl#node> <http://shaclshapes.org/UniversityShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#node> <http://shaclshapes.org/UniversityShape> ;\n" +
                "  <http://www.w3.org/ns/shacl#path> <http://swat.cse.lehigh.edu/onto/univ-bench.owl#doctoralDegreeFrom> .";
        box2.getElement().setProperty("innerHTML", convertNewlinesToHtmlBreaks(text2));

        // Add the boxes to the FlexLayout
        layout.add(box2);
//        String htmlContent = "<div>This is <span style='color: red;'>red</span> and <span style='font-weight: bold;'>bold</span>...</div>";
//
//        Html html = new Html(htmlContent);
//
//        layout.add(html);
        layout.add(new H2("Comparision"));

        DivExample divExample = new DivExample(escapeNewlines(text1), escapeNewlines(text2));
//        layout.add(divExample);
//        add(layout);
        layout.add(divExample);

        // Adjust FlexLayout properties
//        layout.add(box1);
//        layout.add(box2);
//
//        add(name, sayHello);
    }
    public static String escapeNewlines(String input) {
        return input.replaceAll("\n", "\\\\\\\\n");
    }
    public static String convertNewlinesToHtmlBreaks(String input) {
        String s = DivExample.escapeHtmlCharacters(input);
        return s.replaceAll("\n", "<br>");
    }

}
