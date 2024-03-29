package qseevolvingkgwebapp.data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import qseevolvingkgwebapp.services.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Entity
public class ExtractedShapes extends AbstractEntity{

    @ManyToOne
    Version versionEntity;

    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    QseType qseType;

    int support;
    double confidence;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ExtractedShapesClasses")
    List<String> classes;

//    @Lob
//    @Column(name = "fileContent", columnDefinition = "BLOB")
//    byte[] fileContent;

    String fileContentPath;

    //Need to be generated for delete reason during comparison
//    @Lob
//    @Column(name = "fileContentDefault", columnDefinition = "BLOB")
//    byte[] fileContentDefaultShapes;
    String fileContentDefaultShapesPath;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<NodeShape> nodeShapes;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<NodeShape> nodeShapesDefault;

    @Transient
    Model model;

    String comboBoxString;
    @Transient
    Random random = new Random();

    public LocalDateTime getGraphCreationTime() {
        return this.getVersionEntity().getGraph().getCreatedAt();
    }

    public LocalDateTime getVersionCreationTime() {
        return this.getVersionEntity().getCreatedAt();
    }

    public Model getModel() {
        if(model == null) {
            try(FileInputStream inputStream = new FileInputStream(fileContentPath)) {
                this.model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.model;
    }

    public List<NodeShape> getNodeShapes() {
        if(!Hibernate.isInitialized(nodeShapes))
            Hibernate.initialize(nodeShapes);
        return nodeShapes;
    }

    public void setNodeShapes(List<NS> ns) {
        var list = new ArrayList<NodeShape>();
        for(var item : ns) {
            if(item.getSupport() > this.support)
                list.add(new NodeShape(item, this, true));
        }
        this.nodeShapes = list;
    }

    public void setNodeShapesDefault(List<NS> ns) {
        var list = new ArrayList<NodeShape>();
        for(var item : ns) {
            list.add(new NodeShape(item, this, false));
        }
        this.nodeShapesDefault = list;
    }

    public String getClassesAsString() {
        if (classes != null && !classes.isEmpty()) {
            var shortenedList = new ArrayList<>(classes);
            for (int i = 0; i < classes.size(); i++) {
                if(shortenedList.get(i).contains("#"))
                    shortenedList.set(i, shortenedList.get(i).split("#")[1]);
                else
                    shortenedList.set(i, shortenedList.get(i));
            }
            return shortenedList.stream().sorted().collect(Collectors.joining(", "));
        }
        return "";
    }

//    public void setFileContent(byte[] fileContent) {
//        this.fileContent = fileContent;
//    }

    public Version getVersionObject() {
        return versionEntity;
    }

    public void setVersionEntity(Version versionEntity) {
        this.versionEntity = versionEntity;
    }

    public Version getVersionEntity() {
        return versionEntity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public QseType getQseType() {
        return qseType;
    }

    public void setQseType(QseType qseType) {
        this.qseType = qseType;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public boolean isDefaultshape() {
        return confidence == 0 && support == 0;
    }

    public void generateComboBoxString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.comboBoxString = versionEntity.getGraph().getName() + "-" + versionEntity.getVersionNumber() + "-" +
                versionEntity.getName() + "-"
                + formatter.format(createdAt) + "-"
                + qseType + "-" + support + "-" + confidence;
    }

    public String getComboBoxString() {
        return comboBoxString;
    }

//    public byte[] getFileContentDefaultShapes() {
//        return fileContentDefaultShapes;
//    }
//
//    public void setFileContentDefaultShapes(byte[] fileContentDefaultShapes) {
//        this.fileContentDefaultShapes = fileContentDefaultShapes;
//    }

    public List<NodeShape> getNodeShapesDefault() {
        return nodeShapesDefault;
    }

    public String getFileContentPath() {
        return fileContentPath;
    }

    public void setFileContentPath(String fileContentPath) {
        if(!fileContentPath.contains(Utils.shapesPath)) {
            try {
                checkIfShapesDirExists();
                Path sourcePath = Paths.get(fileContentPath);
                String fileName = random.nextInt()+"_"+this.versionEntity.getGraph().getName()+"_"+this.versionEntity.getName()+"_default.ttl";
                Path destinationPath = Paths.get(Utils.getGraphDirectory()+File.separator+Utils.shapesPath+File.separator+fileName);
                Files.copy(sourcePath, destinationPath);
                this.fileContentPath = destinationPath.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            this.fileContentPath = fileContentPath;
    }

    public String getFileContentDefaultShapesPath() {
        return fileContentDefaultShapesPath;
    }

    private static void checkIfShapesDirExists() {
        Path path = Paths.get(Utils.getGraphDirectory()+File.separator+Utils.shapesPath);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFileContentDefaultShapesPath(String fileContentDefaultShapesPath) {
        if(!fileContentDefaultShapesPath.contains(Utils.shapesPath)) {
            checkIfShapesDirExists();
            try {
                Path sourcePath = Paths.get(fileContentDefaultShapesPath);
                String fileName = random.nextInt()+"_"+this.versionEntity.getGraph().getName()+"_"+this.versionEntity.getName()+".ttl";
                Path destinationPath = Paths.get(Utils.getGraphDirectory()+File.separator+Utils.shapesPath+File.separator+fileName);
                Files.copy(sourcePath, destinationPath);
                this.fileContentDefaultShapesPath = destinationPath.toString();
            } catch (IOException e) {
                System.err.println("Error copying file: " + e.getMessage());
            }
        }
        else
            this.fileContentDefaultShapesPath = fileContentDefaultShapesPath;
    }
}
