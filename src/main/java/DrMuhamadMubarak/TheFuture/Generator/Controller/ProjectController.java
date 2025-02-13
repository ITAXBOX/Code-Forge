package DrMuhamadMubarak.TheFuture.Generator.Controller;

import DrMuhamadMubarak.TheFuture.Generator.Enum.ProjectType;
import DrMuhamadMubarak.TheFuture.Generator.Service.ProjectGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class ProjectController {

    private final ProjectGenerator projectGenerator;
    private final ProjectAttributeController projectAttributeController;

    @PostMapping("/generate")
    public String generateProjectStructure(
            @RequestParam("projectName") String projectName,
            @RequestParam("frontendType") String frontendType,
            @RequestParam("backendType") String backendType,
            @RequestParam("databaseType") String databaseType,
            Model model) {
        if (!ProjectType.isValidFrontendType(frontendType) ||
            !ProjectType.isValidBackendType(backendType) ||
            !ProjectType.isValidDatabaseType(databaseType)) {
            model.addAttribute("message", "Invalid project type provided.");
            return "error";
        }

        try {
            projectGenerator.generateProjectStructure(projectName, frontendType, backendType, databaseType);
            model.addAttribute("projectName", projectName);
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            return "error";
        }

        projectAttributeController.resetIndex();
        return "entities";
    }
}