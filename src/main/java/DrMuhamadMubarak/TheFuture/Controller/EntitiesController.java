package DrMuhamadMubarak.TheFuture.Controller;

import DrMuhamadMubarak.TheFuture.Service.EntitiesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class EntitiesController {
    private final EntitiesService entitiesService;

    public EntitiesController(EntitiesService entitiesService) {
        this.entitiesService = entitiesService;
    }

    @PostMapping("/generate-entities")
    public String generateEntities(@RequestParam("projectName") String projectName,
                                   @RequestParam("entities") String entitiesParam,
                                   Model model) {
        try {
            if (entitiesParam == null || entitiesParam.trim().isEmpty()) {
                model.addAttribute("message", "No entities provided.");
                return "error";
            }
            entitiesService.setEntities(entitiesParam.split("\\s*,\\s*")); // Trim whitespace around commas
            String[] entities = entitiesService.getEntities();
            if (entities.length == 0) {
                model.addAttribute("message", "Invalid entity list.");
                return "error";
            }

            entitiesService.generateEntityClasses(projectName, entities);

            for (String entity : entities) {
                entitiesService.generateRepositoryClass(projectName, entity);
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
