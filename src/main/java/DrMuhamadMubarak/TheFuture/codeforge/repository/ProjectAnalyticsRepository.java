package DrMuhamadMubarak.TheFuture.codeforge.repository;

import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectAnalyticsRepository extends CrudRepository<ProjectAnalytics, Long> {

    List<ProjectAnalytics> findTopNByOrderByRequestCountDesc(PageRequest of);

    /**
     * Retrieves top project analytics by request count with eagerly fetched associated projects
     * to avoid N+1 query problem.
     *
     * @param limit The maximum number of results to return
     * @return List of project analytics with their associated projects
     */
    @Query("SELECT pa FROM ProjectAnalytics pa JOIN FETCH pa.project ORDER BY pa.requestCount DESC")
    List<ProjectAnalytics> findTopProjectsWithDetails(PageRequest limit);
}
