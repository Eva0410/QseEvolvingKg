package qseevolvingkgwebapp.data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @Lob
    @Column(name = "fileContent", columnDefinition = "BLOB")
    byte[] fileContent;

    //Need to be generated for delete reason during comparison
    @Lob
    @Column(name = "fileContentDefault", columnDefinition = "BLOB")
    byte[] fileContentDefaultShapes;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<NodeShape> nodeShapes;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<NodeShape> nodeShapesDefault;

    @Transient
    Model model;

    String comboBoxString;

    public LocalDateTime getGraphCreationTime() {
        return this.getVersionEntity().getGraph().getCreatedAt();
    }

    public LocalDateTime getVersionCreationTime() {
        return this.getVersionEntity().getCreatedAt();
    }

    public Model getModel() {
        if(model == null) {
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            try {
                this.model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.model;
    }

    public List<NodeShape> getNodeShapes() {
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
                shortenedList.set(i, shortenedList.get(i).split("#")[1]);
            }
            return shortenedList.stream().sorted().collect(Collectors.joining(", "));
        }
        return "";
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

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

    public byte[] getFileContentDefaultShapes() {
        return fileContentDefaultShapes;
    }

    public void setFileContentDefaultShapes(byte[] fileContentDefaultShapes) {
        this.fileContentDefaultShapes = fileContentDefaultShapes;
    }

    public List<NodeShape> getNodeShapesDefault() {
        return nodeShapesDefault;
    }
}
