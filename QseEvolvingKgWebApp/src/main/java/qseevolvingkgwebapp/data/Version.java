package qseevolvingkgwebapp.data;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Version extends AbstractEntity {

    private int versionNumber;

    private LocalDateTime createdAt;

    @ManyToOne()
    private Graph graph;

    String path;

    String name;

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public String getPath() {
        return path;
    }
    public String getPathWithSpacesForTooltip() {
        StringBuilder builder = new StringBuilder(this.path);
        int interval = 35;
        for (int i = interval; i < builder.length(); i += interval + 1) {
            builder.insert(i, ' ');
        }
        return builder.toString();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
