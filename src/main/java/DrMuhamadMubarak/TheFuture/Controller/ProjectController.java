package DrMuhamadMubarak.TheFuture.Controller;

import DrMuhamadMubarak.TheFuture.Service.ProjectGenerator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class ProjectController {

    private final ProjectGenerator projectGenerator;

    public ProjectController(ProjectGenerator projectGenerator) {
        this.projectGenerator = projectGenerator;
    }

    @PostMapping("/generate")
    public String generateProjectStructure(
            @RequestParam("projectName") String projectName,
            @RequestParam("frontendType") String frontendType,
            @RequestParam("backendType") String backendType,
            @RequestParam("databaseType") String databaseType,
            @RequestParam(value = "entities", required = false) String entitiesParam,
            Model model) {

        try {
            // Generate project structure
            projectGenerator.generateProjectStructure(projectName, frontendType, backendType, databaseType);

            // Save project name and entities to the model
            model.addAttribute("projectName", projectName);
            model.addAttribute("entities", entitiesParam);

            model.addAttribute("message", "Project structure generated successfully. Now add entities.");
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
        }

        return "entities";  // Redirect to a page where entities can be added
    }


}
