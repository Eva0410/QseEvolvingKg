package qseevolvingkgwebapp.views.newgraph;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import javassist.compiler.NoFieldException;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.GraphService;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.services.VersionService;
import qseevolvingkgwebapp.views.MainLayout;

import java.io.*;
import java.time.LocalDateTime;

@PageTitle("New Graph")
@Route(value = "new-graph", layout = MainLayout.class)
@Uses(Icon.class)
public class NewGraphView extends Composite<VerticalLayout> {
    @Autowired()
    private GraphService graphService;

    @Autowired()
    private VersionService versionService;

    public NewGraphView() {
        TextField textField = new TextField();
        textField.setHeight("min-content");
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        textField.setLabel("Name");
        textField.setWidth("min-content");
        buttonPrimary.setText("Upload Graph");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(textField);

        textField.setRequiredIndicatorVisible(true);
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setUploadButton(new Button("Upload .nt file"));

        buttonPrimary.addClickListener(event -> {
            if (textField.getValue().isEmpty()) {
                Notification.show("Name field cannot be empty");
            }
            else if(buffer.getFileName().isEmpty()) {
                Notification.show("Please upload a file");
            }
            else {
                String fileName = buffer.getFileName();
                try (InputStream inputStream = buffer.getInputStream()) {
                    saveFile(inputStream, fileName, textField.getValue());
                    Notification.show("Graph saved!");
                    getUI().ifPresent(ui -> ui.navigate("graphs"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonPrimary.setTooltipText("This will copy the file to the project directory");

        upload.setAcceptedFileTypes(".nt");
        getContent().add(upload);
        getContent().add(buttonPrimary);
    }

    private void saveFile(InputStream inputStream, String fileName, String graphName) throws IOException {
        Graph graph = new Graph();
        graph.setName(graphName);
        graph.setCreatedAt(LocalDateTime.now());
        graph = graphService.insert(graph);

        var dir = Utils.getGraphDirectory();
        Version version = versionService.generateNewVersion(graph);

        String directory = dir+graphName+File.separator;
        String generatedFileName = graphName + "_" + version.getVersionNumber() +".nt";
        String filePath = directory+generatedFileName;
        version.setPath(filePath);
        versionService.update(version);
        File file = new File(directory);

        if (!file.exists()) {
            file.mkdirs();
        }

        File outputFile = new File(directory, generatedFileName);

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

    }
}
