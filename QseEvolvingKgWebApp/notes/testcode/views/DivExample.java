package com.example.application.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@JavaScript("https://cdnjs.cloudflare.com/ajax/libs/jsdiff/5.1.0/diff.js") // Adjust the path to your jsdiff file
public class DivExample extends Div {

    String t1;
    String t2;
    public DivExample(String text1, String text2) {
        t1= text1;
        t2 = text2;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {


        super.onAttach(attachEvent);


        //layout.setSpacing(true);
        //add(layout);

        // Execute JavaScript code when the component is attached to the UI
        UI.getCurrent().getPage().executeJs(
                "const originalText = '"+t1+"';" +
                        "const modifiedText = '"+t2+"';" +
                        "const diff = Diff.diffWords(originalText, modifiedText);" + // Using diffWords for word-level diff
                        "return diff;"
        ).then(response -> {
            if (response instanceof JsonArray) {
                displayDiffOnUI((JsonArray) response);
            }
        });
    }
    private void displayDiffOnUI(JsonArray diffArray) {
        int arraySize = diffArray.length();
        StringBuilder diffText = new StringBuilder();

        for (int i = 0; i < arraySize; i++) {
            JsonValue jsonValue = diffArray.get(i);
            if (jsonValue instanceof JsonObject) {
                JsonObject diffObj = (JsonObject) jsonValue;
                String value = diffObj.getString("value");
                String added = diffObj.hasKey("added") && diffObj.getBoolean("added") ? "added" : "";
                String removed = diffObj.hasKey("removed") && diffObj.getBoolean("removed") ? "removed" : "";

                String color = "";
                String fontWeight = "";

                if (!added.isEmpty() || !removed.isEmpty()) {
                    fontWeight = "font-weight:bold;";
                    color = added.isEmpty() ? removed.isEmpty() ? "" : "color:red;" : "color:green;";
                }
                diffText.append("<span style=\"").append(color).append(fontWeight).append("\">").append(escapeHtmlCharacters(value)).append("</span>");

            }

        }
        Div diffDiv = new Div();
        String s = diffText.toString().replaceAll("\\\\n", "<br>");
        diffDiv.getElement().setProperty("innerHTML", s);
        add(diffDiv);
    }
    public static String escapeHtmlCharacters(String input) {
        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\{", "&#123;")
                .replaceAll("\\}", "&#125;");
        // Add more replacements as needed for other characters
    }
}
