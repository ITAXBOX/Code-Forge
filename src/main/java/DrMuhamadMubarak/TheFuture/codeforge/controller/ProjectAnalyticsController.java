package DrMuhamadMubarak.TheFuture.codeforge.controller;

import DrMuhamadMubarak.TheFuture.codeforge.builder.ProjectAnalyticsResponseDTOBuilder;
import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectAnalyticsResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectAnalyticsService;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectAnalyticsController {

    private final ProjectAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        // Create a response map to hold all dashboard data
        Map<String, Object> dashboardData = new HashMap<>();

        // Get top 5 projects
        List<ProjectAnalytics> topProjects = analyticsService.getTopProjects(5);
        List<ProjectAnalyticsResponseDTO> projectAnalyticsResponseDTOs = topProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList();
        dashboardData.put("topProjects", projectAnalyticsResponseDTOs);

        // Get top frontend framework
        Map<FrontendType, Long> topFrontendFrameworks = analyticsService.getTopFrontendFrameworks(1);
        dashboardData.put("topFrontendFrameworks", topFrontendFrameworks);

        // Get top backend framework
        Map<BackendType, Long> topBackendFrameworks = analyticsService.getTopBackendFrameworks(1);
        dashboardData.put("topBackendFrameworks", topBackendFrameworks);

        // Get top database
        Map<DatabaseType, Long> topDatabases = analyticsService.getTopDatabaseTypes(1);
        dashboardData.put("topDatabases", topDatabases);

        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<ProjectAnalyticsResponseDTO> getProjectAnalytics(@PathVariable Long id) {
        ProjectAnalytics analytics = analyticsService.getProjectAnalytics(id);
        ProjectAnalyticsResponseDTO responseDTO = ProjectAnalyticsResponseDTOBuilder.projectAnalyticsResponseDTObuilder(analytics);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        Map<String, Object> leaderboardData = new HashMap<>();

        // 1. Top 3 projects for special highlighting
        List<ProjectAnalytics> podiumProjects = analyticsService.getTopProjects(3);
        List<ProjectAnalyticsResponseDTO> podiumProjectDTOs = podiumProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList();
        leaderboardData.put("podiumProjects", podiumProjectDTOs);

        // 2. Top 10 projects for complete leaderboard
        List<ProjectAnalytics> leaderboardProjects = analyticsService.getTopProjects(10);
        List<ProjectAnalyticsResponseDTO> leaderboardProjectDTOs = leaderboardProjects.stream()
            .map(ProjectAnalyticsResponseDTOBuilder::projectAnalyticsResponseDTObuilder)
            .toList();
        leaderboardData.put("leaderboardProjects", leaderboardProjectDTOs);

        // 3. Top 5 frontend frameworks with counts
        Map<FrontendType, Long> topFrontendFrameworks = analyticsService.getTopFrontendFrameworks(5);
        leaderboardData.put("topFrontendFrameworks", topFrontendFrameworks);

        // 4. Top 5 backend frameworks with counts
        Map<BackendType, Long> topBackendFrameworks = analyticsService.getTopBackendFrameworks(5);
        leaderboardData.put("topBackendFrameworks", topBackendFrameworks);

        // 5. Top 5 database types with counts
        Map<DatabaseType, Long> topDatabases = analyticsService.getTopDatabaseTypes(5);
        leaderboardData.put("topDatabases", topDatabases);

        // 6. Projects creation timeline (last 6 months)
        Map<String, Long> projectCreationTimeline = analyticsService.getProjectCreationTimeline(6);
        leaderboardData.put("projectCreationTimeline", projectCreationTimeline);

        // 7. Recent activity - recently updated projects
        List<Map<String, Object>> recentActivity = analyticsService.getRecentActivity(5);
        leaderboardData.put("recentActivity", recentActivity);

        // 8. Technology distribution
        Map<String, Long> technologyDistribution = analyticsService.getTechnologyDistribution();
        leaderboardData.put("technologyDistribution", technologyDistribution);

        // 9. Top requested projects by time period (day, week, month)
        Map<String, List<ProjectAnalyticsResponseDTO>> topByTimePeriod = analyticsService.getTopProjectsByTimePeriod();
        leaderboardData.put("topByTimePeriod", topByTimePeriod);

        // 10. Popular technology pairings (which frontend works with which backend)
        List<Map<String, Object>> technologyPairings = analyticsService.getPopularTechnologyPairings(5);
        leaderboardData.put("technologyPairings", technologyPairings);

        return ResponseEntity.ok(leaderboardData);
    }

    @GetMapping("/charts/tech-distribution")
    public ResponseEntity<Map<String, Object>> getTechnologyDistribution() {
        Map<String, Object> chartData = new HashMap<>();

        chartData.put("frontend", analyticsService.getTopFrontendFrameworks(10));
        chartData.put("backend", analyticsService.getTopBackendFrameworks(10));
        chartData.put("database", analyticsService.getTopDatabaseTypes(10));

        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/charts/tech-pairings")
    public ResponseEntity<List<Map<String, Object>>> getTechnologyPairings() {
        return ResponseEntity.ok(analyticsService.getPopularTechnologyPairings(10));
    }

    @GetMapping("/charts/project-timeline")
    public ResponseEntity<Map<String, Long>> getProjectTimeline(@RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(analyticsService.getProjectCreationTimeline(months));
    }

    @GetMapping("/charts/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRecentActivity(limit));
    }
}
