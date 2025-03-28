package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.service.EntityCodeGeneratorService;
import DrMuhamadMubarak.TheFuture.generator.service.EntityJsonProcessorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@AllArgsConstructor
@SessionAttributes({"frontendType", "backendType", "databaseType"})
public class ProjectWorkflowController {
    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final EntityJsonProcessorService entityJsonProcessorService;

    @PostMapping("/generate-entities")
    public String generateEntities(@RequestParam("projectName") String projectName,
                                   @RequestParam("entities") String entitiesParam,
                                   Model model) {
        try {
            String[] entities = entityCodeGeneratorService.processEntities(entitiesParam);
            entityCodeGeneratorService.generateEntityClasses(projectName, entities);
            entityCodeGeneratorService.generateRepositoryClasses(projectName, entities);

            model.addAttribute("projectName", projectName);
            model.addAttribute("entityName", entities[0]);  // Start with the first entity
            return "redirect:/add-attributes?projectName=" + projectName + "&entityName=" + entities[0];
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred while generating entities: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/generate-entities-from-json")
    public String generateProjectFromJson(
            @RequestParam("projectName") String projectName,
            @RequestParam("entitiesJson") String entitiesJson,
            Model model) {

        String frontendType = (String) model.getAttribute("frontendType");
        String backendType = (String) model.getAttribute("backendType");
        String databaseType = (String) model.getAttribute("databaseType");

        return entityJsonProcessorService.processJsonAndGenerateEntities(false, projectName, entitiesJson, model, frontendType, backendType, databaseType, "Project Generated Successfully Using Your JSON.");
    }
}