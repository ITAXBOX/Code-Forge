package DrMuhamadMubarak.TheFuture.generator.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class ProjectAnalytics {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", unique = true)
    private Project project;

    private int requestCount;
    private LocalDateTime lastRequestedAt;

    @PrePersist
    protected void onCreate() {
        lastRequestedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastRequestedAt = LocalDateTime.now();
    }
}

