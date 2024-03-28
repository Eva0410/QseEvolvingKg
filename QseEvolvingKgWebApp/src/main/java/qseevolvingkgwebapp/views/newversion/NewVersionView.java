package qseevolvingkgwebapp.views.newversion;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.GraphService;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.services.VersionService;
import qseevolvingkgwebapp.views.MainLayout;

import java.io.*;

@Route(value = "new-version", layout = MainLayout.class)
@Uses(Icon.class)
public class NewVersionView extends Composite<VerticalLayout> implements HasUrlParameter<Long>, HasDynamicTitle {
    @Autowired()
    private GraphService graphService;

    @Autowired()
    private VersionService versionService;

    private Long graphId;
    private String graphName;

    public NewVersionView() {
        TextField textField = new TextField();
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        buttonPrimary.setText("Upload Graph Version");
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        Utils.setGraphOrVersionGuiFields(textField, buttonPrimary, upload);

        buttonPrimary.addClickListener(event -> {
            if (textField.getValue().isEmpty()) {
                Notification.show("Name field cannot be empty");
            }
            else if(buffer.getFileName().isEmpty()) {
                Notification.show("Please upload a file");
            }else {
                try (InputStream inputStream = buffer.getInputStream()) {
                    saveFile(inputStream, textField.getValue());
                    Notification.show("Graph saved!");
                    getUI().ifPresent(ui -> ui.navigate("versions"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        getContent().add(textField);
        getContent().add(upload);
        getContent().add(buttonPrimary);
    }

    private void saveFile(InputStream inputStream, String versionName) throws IOException {
        Graph graph = graphService.get(this.graphId).get();
        Utils.handleSaveFile(graph, versionService,inputStream, versionName);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {
        graphId = aLong;
        graphName = graphService.get(graphId).get().getName();
    }

    @Override
    public String getPageTitle() {
        return "New Version for graph " + graphName;
    }
}
