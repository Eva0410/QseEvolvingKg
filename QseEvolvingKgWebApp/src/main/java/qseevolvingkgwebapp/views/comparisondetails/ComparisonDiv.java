package qseevolvingkgwebapp.views.comparisondetails;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@JavaScript("https://cdnjs.cloudflare.com/ajax/libs/jsdiff/5.1.0/diff.js") //path to jsdiff file
public class ComparisonDiv extends Div {
    String t1;
    String t2;
    public ComparisonDiv(String text1, String text2) {
        t1= text1;
        t2 = text2;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        updateTextDifferences(t1, t2);
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
        getElement().removeAllChildren();
        Div diffDiv = new Div();
        String s = diffText.toString().replaceAll("\\\\n", "<br>");
        diffDiv.getElement().setProperty("innerHTML", s);
        add(diffDiv);
    }

    public void updateTextDifferences(String t1, String t2) {
        UI.getCurrent().getPage().executeJs(
                "const originalText = '" + t1 + "';" +
                        "const modifiedText = '" + t2 + "';" +
                        "const diff = Diff.diffWords(originalText, modifiedText);" +
                        "return diff;"
        ).then(response -> {
            if (response instanceof JsonArray) {
                displayDiffOnUI((JsonArray) response);
            }
        });
    }
    public static String escapeHtmlCharacters(String input) {
        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\{", "&#123;")
                .replaceAll("\\}", "&#125;");
        // Add more replacements as needed for other characters
    }
}