package qseevolvingkgwebapp.views.newgraph;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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

    private TextField textFieldGraphName;
    private Upload uploadGraphFile;
    private Graph existingGraph;
    private Button buttonSave;

    public NewGraphView() {
        textFieldGraphName = new TextField();
        textFieldGraphName.setHeight("min-content");
        buttonSave = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        textFieldGraphName.setLabel("Name");
        textFieldGraphName.setWidth("min-content");
        buttonSave.setText("Save Graph");
        buttonSave.setWidth("min-content");
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(textFieldGraphName);

        textFieldGraphName.setRequiredIndicatorVisible(true);
        MemoryBuffer buffer = new MemoryBuffer();
        uploadGraphFile = new Upload(buffer);
        uploadGraphFile.setUploadButton(new Button("Upload .nt file"));
        buttonSave.setTooltipText("This will copy the file to the project directory");


        buttonSave.addClickListener(event -> {
            if (textFieldGraphName.getValue().isEmpty()) {
                Notification.show("Name field cannot be empty");
            }
            else if(buffer.getFileName().isEmpty()) {
                Notification.show("Please upload a file");
            }
            else {
                try (InputStream inputStream = buffer.getInputStream()) {
                    saveFile(inputStream, textFieldGraphName.getValue());
                    Notification.show("Graph saved!");
                    getUI().ifPresent(ui -> ui.navigate("graphs"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        uploadGraphFile.setAcceptedFileTypes(".nt");
        getContent().add(uploadGraphFile);
        getContent().add(buttonSave);
    }

    private void saveFile(InputStream inputStream, String graphName) throws IOException {
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
        version.setName("Original");
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
