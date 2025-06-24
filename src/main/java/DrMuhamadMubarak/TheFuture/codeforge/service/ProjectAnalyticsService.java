package DrMuhamadMubarak.TheFuture.codeforge.service;

import DrMuhamadMubarak.TheFuture.codeforge.builder.ProjectAnalyticsResponseDTOBuilder;
import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectAnalyticsResponseDTO;
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

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public void recordProjectAccess(Project project) {
        ProjectAnalytics analytics = project.getAnalytics();

        if (analytics == null) {
            analytics = new ProjectAnalytics();
            project.setAnalytics(analytics);
        }

        analytics.incrementRequestCount();
        analyticsRepository.save(analytics);
    }

    /**
     * Gets the top N projects by request count.
     * Uses an optimized query that eagerly fetches the associated Project data
     * to avoid N+1 query problems....
     *
     * @param limit the maximum number of projects to return
     * @return list of ProjectAnalytics with eagerly loaded Project data
     */
    public List<ProjectAnalytics> getTopProjects(int limit) {
        return analyticsRepository.findTopProjectsWithDetails(PageRequest.of(0, limit));
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

    /**
     * Gets a timeline of project creation over a number of months
     * @param months Number of months to include
     * @return Map with month (in format MMM yyyy) as key and count as value
     */
    public Map<String, Long> getProjectCreationTimeline(int months) {
        List<Project> allProjects = projectRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Long> timeline = new LinkedHashMap<>();

        // Generate the last N months
        LocalDateTime now = LocalDateTime.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            timeline.put(month.format(formatter), 0L);
        }

        // Count projects per month
        for (Project project : allProjects) {
            if (project.getCreatedAt() != null) {
                LocalDateTime creationDate = project.getCreatedAt();
                if (creationDate.isAfter(now.minusMonths(months))) {
                    String monthKey = YearMonth.from(creationDate).format(formatter);
                    timeline.put(monthKey, timeline.getOrDefault(monthKey, 0L) + 1);
                }
            }
        }

        return timeline;
    }

    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<ProjectAnalytics> recentAnalytics = new ArrayList<>();
        for (ProjectAnalytics pa : analyticsRepository.findAll()) {
            if (pa.getLastRequestedAt() != null) {
                recentAnalytics.add(pa);
            }
        }
        recentAnalytics.sort(Comparator.comparing(ProjectAnalytics::getLastRequestedAt).reversed());
        if (recentAnalytics.size() > limit) {
            recentAnalytics = recentAnalytics.subList(0, limit);
        }

        List<Map<String, Object>> activity = new ArrayList<>();
        for (ProjectAnalytics analytics : recentAnalytics) {
            Project project = analytics.getProject();
            if (project != null) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("projectId", project.getId());
                entry.put("projectName", project.getName());
                entry.put("timestamp", analytics.getLastRequestedAt());
                entry.put("action", "Project viewed");
                entry.put("requestCount", analytics.getRequestCount());
                activity.add(entry);
            }
        }

        return activity;
    }

    /**
     * Get all technology distributions in one map
     * @return Map of technology types and their counts
     */
    public Map<String, Long> getTechnologyDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        // Frontend technologies
        Map<FrontendType, Long> frontendCounts = projectRepository.countProjectsByFrontendType().stream()
            .collect(Collectors.toMap(
                ProjectRepository.FrontendTypeCount::getFrontendType,
                ProjectRepository.FrontendTypeCount::getCount
            ));

        for (Map.Entry<FrontendType, Long> entry : frontendCounts.entrySet()) {
            distribution.put("Frontend: " + entry.getKey().name(), entry.getValue());
        }

        // Backend technologies
        Map<BackendType, Long> backendCounts = projectRepository.countProjectsByBackendType().stream()
            .collect(Collectors.toMap(
                ProjectRepository.BackendTypeCount::getBackendType,
                ProjectRepository.BackendTypeCount::getCount
            ));

        for (Map.Entry<BackendType, Long> entry : backendCounts.entrySet()) {
            distribution.put("Backend: " + entry.getKey().name(), entry.getValue());
        }

        // Database technologies
        Map<DatabaseType, Long> databaseCounts = projectRepository.countProjectsByDatabaseType().stream()
            .collect(Collectors.toMap(
                ProjectRepository.DatabaseTypeCount::getDatabaseType,
                ProjectRepository.DatabaseTypeCount::getCount
            ));

        for (Map.Entry<DatabaseType, Long> entry : databaseCounts.entrySet()) {
            distribution.put("Database: " + entry.getKey().name(), entry.getValue());
        }

        return distribution;
    }

    /**
     * Get top projects by different time periods
     * @return Map with time periods as keys and lists of top projects as values
     */
    public Map<String, List<ProjectAnalyticsResponseDTO>> getTopProjectsByTimePeriod() {
        Map<String, List<ProjectAnalyticsResponseDTO>> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Today's top projects
        List<ProjectAnalytics> todayProjects = new ArrayList<>();
        for (ProjectAnalytics pa : analyticsRepository.findAll()) {
            if (pa.getLastRequestedAt() != null &&
                pa.getLastRequestedAt().isAfter(now.withHour(0).withMinute(0).withSecond(0))) {
                todayProjects.add(pa);
            }
        }
        todayProjects.sort(Comparator.comparing(ProjectAnalytics::getRequestCount).reversed());
        if (todayProjects.size() > 3) {
            todayProjects = todayProjects.subList(0, 3);
        }
        result.put("today", todayProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList());

        // This week's top projects
        List<ProjectAnalytics> weekProjects = new ArrayList<>();
        for (ProjectAnalytics pa : analyticsRepository.findAll()) {
            if (pa.getLastRequestedAt() != null &&
                pa.getLastRequestedAt().isAfter(now.minusDays(7))) {
                weekProjects.add(pa);
            }
        }
        weekProjects.sort(Comparator.comparing(ProjectAnalytics::getRequestCount).reversed());
        if (weekProjects.size() > 3) {
            weekProjects = weekProjects.subList(0, 3);
        }
        result.put("week", weekProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList());

        // This month's top projects
        List<ProjectAnalytics> monthProjects = new ArrayList<>();
        for (ProjectAnalytics pa : analyticsRepository.findAll()) {
            if (pa.getLastRequestedAt() != null &&
                pa.getLastRequestedAt().isAfter(now.minusMonths(1))) {
                monthProjects.add(pa);
            }
        }
        monthProjects.sort(Comparator.comparing(ProjectAnalytics::getRequestCount).reversed());
        if (monthProjects.size() > 3) {
            monthProjects = monthProjects.subList(0, 3);
        }
        result.put("month", monthProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList());

        return result;
    }

    /**
     * Get popular technology stack combinations
     * @param limit Number of pairings to return
     * @return List of technology pairing details
     */
    public List<Map<String, Object>> getPopularTechnologyPairings(int limit) {
        // Count each unique frontend+backend+database combination
        Map<String, Integer> pairingCounts = new HashMap<>();
        Map<String, List<String>> pairingDetails = new HashMap<>();

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            if (project.getFrontendType() != null && project.getBackendType() != null && project.getDatabaseType() != null) {
                String key = project.getFrontendType() + "-" + project.getBackendType() + "-" + project.getDatabaseType();
                pairingCounts.put(key, pairingCounts.getOrDefault(key, 0) + 1);

                List<String> details = new ArrayList<>();
                details.add(project.getFrontendType().name());
                details.add(project.getBackendType().name());
                details.add(project.getDatabaseType().name());
                pairingDetails.put(key, details);
            }
        }

        // Sort by count and take the top N
        return pairingCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> result = new HashMap<>();
                List<String> details = pairingDetails.get(entry.getKey());

                result.put("frontend", details.get(0));
                result.put("backend", details.get(1));
                result.put("database", details.get(2));
                result.put("count", entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }
}
