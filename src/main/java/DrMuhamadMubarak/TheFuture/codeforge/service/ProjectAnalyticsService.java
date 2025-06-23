package DrMuhamadMubarak.TheFuture.codeforge.service;

import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import DrMuhamadMubarak.TheFuture.codeforge.repository.ProjectAnalyticsRepository;
import DrMuhamadMubarak.TheFuture.codeforge.repository.ProjectRepository;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectAnalyticsService {

    private final ProjectAnalyticsRepository analyticsRepository;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    public ProjectAnalytics getProjectAnalytics(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        return project.getAnalytics();
    }

    public void recordProjectAccess(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        ProjectAnalytics analytics = project.getAnalytics();

        if (analytics == null) {
            analytics = new ProjectAnalytics();
            project.setAnalytics(analytics);
        }

        analytics.incrementRequestCount();
        analyticsRepository.save(analytics);
    }

    public List<ProjectAnalytics> getTopProjects(int limit) {
        return analyticsRepository.findTopNByOrderByRequestCountDesc(PageRequest.of(0, limit));
    }

    // Get most popular frontend frameworks
    public Map<FrontendType, Long> getTopFrontendFrameworks(int limit) {
        return projectRepository.countProjectsByFrontendType().stream().sorted((a, b) -> Long.compare(b.getCount(), a.getCount())).limit(limit).collect(Collectors.toMap(ProjectRepository.FrontendTypeCount::getFrontendType, ProjectRepository.FrontendTypeCount::getCount, (a, _) -> a, LinkedHashMap::new));
    }

    // Get most popular backend frameworks
    public Map<BackendType, Long> getTopBackendFrameworks(int limit) {
        return projectRepository.countProjectsByBackendType().stream().sorted((a, b) -> Long.compare(b.getCount(), a.getCount())).limit(limit).collect(Collectors.toMap(ProjectRepository.BackendTypeCount::getBackendType, ProjectRepository.BackendTypeCount::getCount, (a, _) -> a, LinkedHashMap::new));
    }

    // Get most popular databases
    public Map<DatabaseType, Long> getTopDatabaseTypes(int limit) {
        return projectRepository.countProjectsByDatabaseType().stream().sorted((a, b) -> Long.compare(b.getCount(), a.getCount())).limit(limit).collect(Collectors.toMap(ProjectRepository.DatabaseTypeCount::getDatabaseType, ProjectRepository.DatabaseTypeCount::getCount, (a, _) -> a, LinkedHashMap::new));
    }
}
