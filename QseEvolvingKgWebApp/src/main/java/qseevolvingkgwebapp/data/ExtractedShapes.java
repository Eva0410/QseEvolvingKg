package qseevolvingkgwebapp.data;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

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
}
