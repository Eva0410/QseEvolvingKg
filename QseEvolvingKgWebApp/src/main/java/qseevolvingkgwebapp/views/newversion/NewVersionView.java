package qseevolvingkgwebapp.views.newversion;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.springframework.beans.factory.annotation.Autowired;
import qseevolvingkgwebapp.data.Graph;
import qseevolvingkgwebapp.data.Version;
import qseevolvingkgwebapp.services.GraphService;
import qseevolvingkgwebapp.services.Utils;
import qseevolvingkgwebapp.services.VersionService;
import qseevolvingkgwebapp.views.MainLayout;

import java.io.*;
import java.time.LocalDateTime;

@Route(value = "new-version", layout = MainLayout.class)
@Uses(Icon.class)

public class NewVersionView extends Composite<VerticalLayout> implements HasUrlParameter<Long>, AfterNavigationListener {
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
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        textField.setLabel("Name");
        textField.setWidth("min-content");
        textField.setHeight("min-content");
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
        buttonPrimary.setTooltipText("This will copy the file to the project directory");

        upload.setAcceptedFileTypes(".nt");
        getContent().add(textField);
        getContent().add(upload);
        getContent().add(buttonPrimary);
    }

    private void saveFile(InputStream inputStream, String versionName) throws IOException {
        Graph graph = graphService.get(this.graphId).get();

        var dir = Utils.getGraphDirectory();
        Version version = versionService.generateNewVersion(graph);
        version.setName(versionName);

        String directory = dir+graph.getName()+File.separator;
        String generatedFileName = graph.getName() + "_" + version.getVersionNumber() +".nt";
        String filePath = directory+generatedFileName;
        version.setPath(filePath);
        versionService.update(version);
        File file = new File(directory);

        File outputFile = new File(directory, generatedFileName);

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {
        graphId = aLong;
        graphName = graphService.get(graphId).get().getName();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        getUI().get().getPage().setTitle("New Version for graph " + graphName);
    }
}
