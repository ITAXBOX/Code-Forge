package DrMuhamadMubarak.TheFuture.codeforge.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project_analytics", indexes = {
    @Index(name = "idx_project_analytics_last_requested", columnList = "lastRequestedAt"),
    @Index(name = "idx_project_analytics_request_count", columnList = "requestCount")
})
public class ProjectAnalytics {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", unique = true)
    @Setter
    @JsonBackReference
    private Project project;

    @Column(nullable = false)
    private int requestCount;
    @Getter
    private LocalDateTime lastRequestedAt;

    public void incrementRequestCount() {
        this.requestCount++;
        this.lastRequestedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        lastRequestedAt = LocalDateTime.now();
        requestCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastRequestedAt = LocalDateTime.now();
    }
}
