package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.ai.AIService;
import DrMuhamadMubarak.TheFuture.generator.service.EntityJsonProcessorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import static DrMuhamadMubarak.TheFuture.generator.ai.ThePrompt.*;

@Controller
@AllArgsConstructor
@SessionAttributes({"frontendType", "backendType", "databaseType", "projectDescription"})
public class AIProjectController {
    private final EntityJsonProcessorService entityJsonProcessorService;
    private final AIService aiService;

    @PostMapping("/generate-entities-from-prompt")
    public String generateProjectFromPrompt(
            @RequestParam("projectName") String projectName,
            Model model) {
        String projectDescription = (String) model.getAttribute("projectDescription");

        try {
            // Step 1: Get entity names based on the project topic
            String entityNamesPrompt;
            if (projectDescription != null && !projectDescription.isEmpty()) {
                entityNamesPrompt = String.format(ENTITY_NAMES_PROMPT_WITH_DESCRIPTION, projectName, projectDescription);
            } else {
                entityNamesPrompt = String.format(ENTITY_NAMES_PROMPT, projectName);
            }

            String entityNamesJson = aiService.generateJsonFromPrompt(entityNamesPrompt).block();

            if (entityNamesJson == null || entityNamesJson.isEmpty()) {
                model.addAttribute("message", "Failed to generate entity names.");
                return "error";
            }

            // Step 2: Use entity names to generate and validate entity definitions
            String entityDefinitionAndFixPrompt = String.format(ENTITY_DEFINITION_AND_FIX_PROMPT, entityNamesJson);
            String fixedEntitiesJson = aiService.generateJsonFromPrompt(entityDefinitionAndFixPrompt).block();

            if (fixedEntitiesJson == null || fixedEntitiesJson.isEmpty()) {
                model.addAttribute("message", "Failed to generate and validate entity definitions.");
                return "error";
            }

            String frontendType = (String) model.getAttribute("frontendType");
            String backendType = (String) model.getAttribute("backendType");
            String databaseType = (String) model.getAttribute("databaseType");

            // Step 3: Process the validated JSON and generate the project
            return entityJsonProcessorService.processJsonAndGenerateEntities(projectName, fixedEntitiesJson, model,
                    frontendType, backendType, databaseType, "Project Generated Successfully Using OpenAI.");
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred while generating JSON from prompt: " + e.getMessage());
            return "error";
        }
    }
}