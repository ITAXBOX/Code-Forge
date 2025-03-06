package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.service.ProjectEntitiesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class ProjectEntitiesController {
    private final ProjectEntitiesService projectEntitiesService;

    @PostMapping("/generate-entities")
    public String generateEntities(@RequestParam("projectName") String projectName,
                                   @RequestParam("entities") String entitiesParam,
                                   Model model) {
        try {
            String[] entities = projectEntitiesService.processEntities(entitiesParam);
            projectEntitiesService.generateEntityClasses(projectName, entities);
            projectEntitiesService.generateRepositoryClasses(projectName, entities);

            model.addAttribute("projectName", projectName);
            model.addAttribute("entityName", entities[0]);  // Start with the first entity
            return "redirect:/add-attributes?projectName=" + projectName + "&entityName=" + entities[0];
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred while generating entities: " + e.getMessage());
            return "error";
        }
    }
}