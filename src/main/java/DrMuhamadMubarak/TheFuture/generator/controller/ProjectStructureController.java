package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectService;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import DrMuhamadMubarak.TheFuture.generator.service.ProjectStructureService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.IOException;

@Controller
@AllArgsConstructor
@SessionAttributes({"projectName", "frontendType", "backendType", "databaseType", "projectDescription"})
public class ProjectStructureController {

    private final ProjectStructureService projectStructureService;
    private final ProjectService projectService;

    @PostMapping("/generate")
    public String generateProjectStructure(
            @RequestParam("projectName") String projectName,
            @RequestParam("frontendType") String frontendType,
            @RequestParam("backendType") String backendType,
            @RequestParam("databaseType") String databaseType,
            @RequestParam(value = "projectDescription", required = false) String projectDescription,
            Model model) {
        if (!FrontendType.isValid(frontendType) ||
            !BackendType.isValid(backendType) ||
            !DatabaseType.isValid(databaseType)) {
            model.addAttribute("message", "Invalid project type provided.");
            return "error";
        }
        try {
            if(projectService.isProjectExists(projectName)) {
                model.addAttribute("message", "Project with this name already exists.");
                return "leaderboard";
            }
            projectStructureService.generateProjectStructure(projectName, frontendType, backendType, databaseType);
            model.addAttribute("projectName", projectName);
            model.addAttribute("frontendType", frontendType);
            model.addAttribute("backendType", backendType);
            model.addAttribute("databaseType", databaseType);
            model.addAttribute("projectDescription", projectDescription);
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            return "error";
        }
        return "entities";
    }
}