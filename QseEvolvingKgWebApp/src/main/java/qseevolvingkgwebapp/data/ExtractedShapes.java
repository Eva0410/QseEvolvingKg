package qseevolvingkgwebapp.data;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


    public String getClassesAsString() {
        if (classes != null && !classes.isEmpty()) {
            var shortenedList = new ArrayList<String>(classes);
            for (int i = 0; i < classes.size(); i++) {
                shortenedList.set(i, shortenedList.get(i).split("#")[1]);
            }
            return String.join(", ", shortenedList);
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
