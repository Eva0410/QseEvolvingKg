package qseevolvingkgwebapp.data;

import cs.qse.common.structure.NS;
import jakarta.persistence.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.w3c.dom.Node;

import javax.xml.stream.Location;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<NodeShape> nodeShapes;

    @Transient
    Model model;


    public LocalDateTime getGraphCreationTime() {
        return this.getVersionEntity().getGraph().getCreatedAt();
    }

    public LocalDateTime getVersionCreationTime() {
        return this.getVersionEntity().getCreatedAt();
    }

    public Model getModel() {
        if(model == null) {
//            Model m = ModelFactory.createDefaultModel();
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            try {
                this.model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            this.model =  m.read(inputStream, null, "TTL");
        }
        return this.model;
    }

    public List<NodeShape> getNodeShapes() {
        return nodeShapes;
    }

    public void setNodeShapes(List<NS> ns) {
        var list = new ArrayList<NodeShape>();
        for(var item : ns) {
            list.add(new NodeShape(item, this));
        }
        this.nodeShapes = list;
    }

    public String getClassesAsString() {
        if (classes != null && !classes.isEmpty()) {
            var shortenedList = new ArrayList<>(classes);
            for (int i = 0; i < classes.size(); i++) {
                shortenedList.set(i, shortenedList.get(i).split("#")[1]);
            }
            return String.join(", ", shortenedList.stream().sorted().collect(Collectors.toList()));
        }
        return "";
    }
    public byte[] getFileContent() {
        return fileContent;
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

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public boolean isDefaultshape() {
        return confidence == 0 && support == 0;
    }
}
