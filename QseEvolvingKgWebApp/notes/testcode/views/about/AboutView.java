package com.example.application.views.about;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import java.util.LinkedList;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

    private TextArea textArea1;
    private TextArea textArea2;
    private Binder<TextComparisonModel> binder;


    public AboutView() {
        setSpacing(false);

        Image img = new Image("images/empty-plant.png", "placeholder plant");
        img.setWidth("200px");
        add(img);

        H2 header = new H2("This place intentionally left empty");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        add(header);
        add(new Paragraph("It’s a place where you can grow your own UI 🤗"));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");

        textArea1 = new TextArea("Text 1");
        textArea2 = new TextArea("Text 2");

        binder = new Binder<>(TextComparisonModel.class);

        binder.bind(textArea1, TextComparisonModel::getText1, TextComparisonModel::setText1);
        binder.bind(textArea2, TextComparisonModel::getText2, TextComparisonModel::setText2);

        Button compareButton = new Button("Compare Texts", event -> compareTexts());

        HorizontalLayout textLayout = new HorizontalLayout(textArea1, textArea2);
        textLayout.setWidth("100%");

        add(textLayout, compareButton);

        // Create an HTML string with different styles
        String htmlContent = "<div>This is <span style='color: red;'>red</span> and <span style='font-weight: bold;'>bold</span>...</div>";

        // Create an Html component with the HTML content
        Html html = new Html(htmlContent);

        // Add the Html component to the layout
        add(html);

//        Span text = new Span("This is ");
//        Span redText = new Span("red");
//        redText.getStyle().set("color", "red");
//        Span boldText = new Span(" and ");
//        boldText.getStyle().set("font-weight", "bold");
//        Span plainText = new Span(".");
//
//        // Add the Span components to the layout
//        add(text, redText, boldText, plainText);
    }

    private void compareTexts() {

        binder = new Binder<>(TextComparisonModel.class);

        binder.bind(textArea1, TextComparisonModel::getText1, TextComparisonModel::setText1);
        binder.bind(textArea2, TextComparisonModel::getText2, TextComparisonModel::setText2);

        //TextComparisonModel model = binder.getBean();
        String text1 = textArea1.getValue();
        String text2 = textArea2.getValue();

        LinkedList<Character> differences = new LinkedList<>();
        textArea2.setValue("asdf<span style='background-color: #ffd1d1;'>asdf</span>");

        // Find differences
//        int minLength = Math.min(text1.length(), text2.length());
//        for (int i = 0; i < minLength; i++) {
//            if (text1.charAt(i) != text2.charAt(i)) {
//                differences.add(text2.charAt(i));
//            }
//        }
//
//        // Apply highlighting to textArea2
//        StringBuilder highlightedText2 = new StringBuilder();
//        for (int i = 0; i < text2.length(); i++) {
//            char currentChar = text2.charAt(i);
//            if (differences.contains(currentChar)) {
//                highlightedText2.append("<span style='background-color: #ffd1d1;'>")
//                        .append(currentChar)
//                        .append("</span>");
//            } else {
//                highlightedText2.append(currentChar);
//            }
//        }
//        textArea2.getElement().setProperty("innerHTML", highlightedText2.toString());
//
//        // Notify the user
//        if (differences.isEmpty()) {
//            Notification.show("Texts are identical!");
//        } else {
//            Notification.show("Texts have differences.");
//        }
    }

}

