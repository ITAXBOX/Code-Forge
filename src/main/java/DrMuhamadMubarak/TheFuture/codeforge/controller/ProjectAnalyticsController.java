package DrMuhamadMubarak.TheFuture.codeforge.controller;

import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectAnalyticsService;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/analytics")
@AllArgsConstructor
public class ProjectAnalyticsController {

    private final ProjectAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public void getDashboard(Model model) {
        // Get top 5 projects
        List<ProjectAnalytics> topProjects = analyticsService.getTopProjects(5);
        model.addAttribute("topProjects", topProjects);

        // Get top frontend framework
        Map<FrontendType, Long> topFrontendFrameworks = analyticsService.getTopFrontendFrameworks(1);
        model.addAttribute("topFrontendFrameworks", topFrontendFrameworks);

        // Get top backend framework
        Map<BackendType, Long> topBackendFrameworks = analyticsService.getTopBackendFrameworks(1);
        model.addAttribute("topBackendFrameworks", topBackendFrameworks);

        // Get top database
        Map<DatabaseType, Long> topDatabases = analyticsService.getTopDatabaseTypes(1);
        model.addAttribute("topDatabases", topDatabases);
    }

    @GetMapping("/project/{id}")
    public void getProjectAnalytics(@PathVariable Long id, Model model) {
        ProjectAnalytics analytics = analyticsService.getProjectAnalytics(id);
        model.addAttribute("analytics", analytics);
    }
}
