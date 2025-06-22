package DrMuhamadMubarak.TheFuture.codeforge.repository;

import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectAnalyticsRepository extends CrudRepository<ProjectAnalytics, Long> {

    List<ProjectAnalytics> findTopNByOrderByRequestCountDesc(PageRequest of);
}
