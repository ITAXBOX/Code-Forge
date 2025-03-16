package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.ai.AIService;
import DrMuhamadMubarak.TheFuture.generator.service.ProjectEntityGenerationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static DrMuhamadMubarak.TheFuture.generator.ai.ThePrompt.PROMPT_TEMPLATE;

@Controller
@AllArgsConstructor
public class AIProjectController {
    private final ProjectEntityGenerationService projectEntityGenerationService;
    private final AIService aiService;

    @PostMapping("/generate-entities-from-prompt")
    public String generateProjectFromPrompt(
            @RequestParam("projectName") String projectName,
            Model model) {

        try {
            String prompt = String.format(PROMPT_TEMPLATE, projectName);

            String entitiesJson = aiService.generateJsonFromPrompt(prompt).block();

            return projectEntityGenerationService.processJsonAndGenerateEntities(projectName, entitiesJson, model, "Project Generated Successfully Using OpenAi.");
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred while generating JSON from prompt: " + e.getMessage());
            return "error";
        }
    }
}