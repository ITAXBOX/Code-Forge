package DrMuhamadMubarak.TheFuture.codeforge.repository;

import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p.frontendType as frontendType, COUNT(p) as count FROM Project p GROUP BY p.frontendType")
    List<FrontendTypeCount> countProjectsByFrontendType();

    @Query("SELECT p.backendType as backendType, COUNT(p) as count FROM Project p GROUP BY p.backendType")
    List<BackendTypeCount> countProjectsByBackendType();

    @Query("SELECT p.databaseType as databaseType, COUNT(p) as count FROM Project p GROUP BY p.databaseType")
    List<DatabaseTypeCount> countProjectsByDatabaseType();

    boolean existsByName(@NotBlank @Size(min = 3, max = 50) String name);

    // Projections for the query results
    interface FrontendTypeCount {
        FrontendType getFrontendType();

        Long getCount();
    }

    interface BackendTypeCount {
        BackendType getBackendType();

        Long getCount();
    }

    interface DatabaseTypeCount {
        DatabaseType getDatabaseType();

        Long getCount();
    }
}
