package qseevolvingkgwebapp.data;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class ExtractedShapes extends AbstractEntity{

    @ManyToOne
    Version version;

    LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    QseType qseType;

    int support;
    double confidence;

    @ElementCollection
    @CollectionTable(name = "ExtractedShapesClasses")
    List<String> classes;

    public Version getVersionObject() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Version getVersionEntity() {
        return version;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
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
