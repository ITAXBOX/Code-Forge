package DrMuhamadMubarak.TheFuture.Generator.Controller;

import DrMuhamadMubarak.TheFuture.Generator.Service.ProjectEntitiesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class ProjectEntitiesController {
    private final ProjectEntitiesService projectEntitiesService;

    @PostMapping("/generate-entities")
    public String generateEntities(@RequestParam("projectName") String projectName,
                                   @RequestParam("entities") String entitiesParam,
                                   Model model) {
        try {
            if (entitiesParam == null || entitiesParam.trim().isEmpty()) {
                model.addAttribute("message", "No entities provided.");
                return "error";
            }
            projectEntitiesService.setEntities(entitiesParam.split("\\s*,\\s*")); // Trim whitespace around commas
            String[] entities = projectEntitiesService.getEntities();

            projectEntitiesService.generateEntityClasses(projectName, entities);

            for (String entity : entities) {
                projectEntitiesService.generateRepositoryClass(projectName, entity);
            }

            model.addAttribute("projectName", projectName);
            model.addAttribute("entityName", entities[0]);  // Start with the first entity
            return "redirect:/add-attributes?projectName=" + projectName + "&entityName=" + entities[0];
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred while generating entities: " + e.getMessage());
            return "error";
        }
    }
}
