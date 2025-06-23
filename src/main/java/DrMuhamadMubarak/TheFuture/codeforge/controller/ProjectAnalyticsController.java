package DrMuhamadMubarak.TheFuture.codeforge.controller;

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
        dashboardData.put("topProjects", topProjects);

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
    public ResponseEntity<ProjectAnalytics> getProjectAnalytics(@PathVariable Long id) {
        ProjectAnalytics analytics = analyticsService.getProjectAnalytics(id);
        return ResponseEntity.ok(analytics);
    }
}
