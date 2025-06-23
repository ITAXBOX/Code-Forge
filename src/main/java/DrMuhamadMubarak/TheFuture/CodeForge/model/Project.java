package DrMuhamadMubarak.TheFuture.codeforge.model;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Projects",
        indexes = @Index(name = "idx_project_name", columnList = "name")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 50, message = "Project name must be between 3-50 characters")
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrontendType frontendType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackendType backendType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType databaseType;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private ProjectAnalytics analytics;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Project(String name, String description, FrontendType frontendType, BackendType backendType, DatabaseType databaseType) {
        this.name = name;
        this.description = description;
        this.frontendType = frontendType;
        this.backendType = backendType;
        this.databaseType = databaseType;
    }

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
