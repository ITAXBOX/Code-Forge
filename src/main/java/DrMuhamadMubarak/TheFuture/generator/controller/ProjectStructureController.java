package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import DrMuhamadMubarak.TheFuture.generator.service.ProjectStructureService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class ProjectStructureController {

    private final ProjectStructureService projectStructureService;

    @PostMapping("/generate")
    public String generateProjectStructure(
            @RequestParam("projectName") String projectName,
            @RequestParam("frontendType") String frontendType,
            @RequestParam("backendType") String backendType,
            @RequestParam("databaseType") String databaseType,
            Model model) {
        if (!FrontendType.isValid(frontendType) ||
            !BackendType.isValid(backendType) ||
            !DatabaseType.isValid(databaseType)) {
            model.addAttribute("message", "Invalid project type provided.");
            return "error";
        }

        try {
            projectStructureService.generateProjectStructure(projectName, frontendType, backendType, databaseType);
            model.addAttribute("projectName", projectName);
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            return "error";
        }

        return "entities";
    }

    @GetMapping("/loading-page")
    public String loadingPage() {
        return "loading-page";
    }
}