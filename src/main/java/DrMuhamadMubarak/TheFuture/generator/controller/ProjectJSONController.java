package DrMuhamadMubarak.TheFuture.generator.controller;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.generator.service.ProjectAttributeService;
import DrMuhamadMubarak.TheFuture.generator.service.ProjectEntitiesService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class ProjectJSONController {
    private final ProjectEntitiesService projectEntitiesService;
    private final ProjectAttributeService projectAttributeService;

    @PostMapping("/generate-entities-from-json")
    public String generateEntitiesFromJson(
            @RequestParam("projectName") String projectName,
            @RequestParam("entitiesJson") String entitiesJson,
            Model model) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(entitiesJson);
            JsonNode entitiesNode = rootNode.path("entities");

            if (entitiesNode.isMissingNode() || !entitiesNode.isArray()) {
                model.addAttribute("message", "Invalid JSON format: 'entities' array is missing or invalid.");
                return "error";
            }

            for (JsonNode entityNode : entitiesNode) {
                String entityName = entityNode.path("name").asText();
                JsonNode attributesNode = entityNode.path("attributes");

                if (attributesNode.isMissingNode() || !attributesNode.isArray()) {
                    model.addAttribute("message", "Invalid JSON format: 'attributes' array is missing or invalid for entity: " + entityName);
                    return "error";
                }

                projectEntitiesService.generateEntityClass(projectName, entityName);
                projectEntitiesService.generateRepositoryClass(projectName, entityName);

                for (JsonNode attributeNode : attributesNode) {
                    AttributeDTO attribute = objectMapper.treeToValue(attributeNode, AttributeDTO.class);

                    projectAttributeService.addAttributesToEntity(projectName, entityName, attribute);
                }

                projectEntitiesService.generateServiceClass(projectName, entityName, projectAttributeService.getAttributes());
                projectEntitiesService.generateControllerClass(projectName, entityName);
                projectEntitiesService.generateUI(projectName, entityName, projectAttributeService.getAttributes());

                projectAttributeService.clearAttributes();
            }

            projectEntitiesService.generateSecurityClass(projectName);
            projectEntitiesService.generateDataInitializerClass(projectName);

            model.addAttribute("message", "Entities generated successfully from JSON.");
            return "result";
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred while processing JSON: " + e.getMessage());
            return "error";
        }
    }
}
