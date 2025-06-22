package DrMuhamadMubarak.TheFuture.generator.model;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "Projects",
        indexes = @Index(name = "idx_project_name", columnList = "name")
)
@Getter
public class Project {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrontendType frontendType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackendType backendType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType databaseType;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commit> commits = new ArrayList<>();

    public void addCommit(Commit commit) {
        commits.add(commit);
        commit.setProject(this); // Keep bidirectional sync
    }

    public void removeCommit(Commit commit) {
        commits.remove(commit);
        commit.setProject(null);
    }

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private ProjectAnalytics analytics;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Project(String name, FrontendType frontendType, BackendType backendType, DatabaseType databaseType) {
        this.name = name;
        this.frontendType = frontendType;
        this.backendType = backendType;
        this.databaseType = databaseType;
        this.commits = new ArrayList<>();
    }

    protected Project() {} // Default constructor for JPA

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
