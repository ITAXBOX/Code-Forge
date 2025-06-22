package DrMuhamadMubarak.TheFuture.generator.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Commits")
public class Commit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String commitHash;

    private LocalDateTime committedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @Setter
    private Project project;

    // Constructor to set mandatory fields
    public Commit(String message, String commitHash, Project project) {
        this.message = message;
        this.commitHash = commitHash;
        this.project = project;
    }

    protected Commit() {} // For JPA

    @PrePersist
    protected void onCommit() {
        committedAt = LocalDateTime.now();
    }
}


