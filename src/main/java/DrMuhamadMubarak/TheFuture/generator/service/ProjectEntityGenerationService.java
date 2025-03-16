package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;

@Service
@AllArgsConstructor
public class ProjectEntityGenerationService {

    private final ProjectEntitiesService projectEntitiesService;
    private final ProjectAttributesService projectAttributesService;

    public String processJsonAndGenerateEntities(String projectName, String json, Model model, String successMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode entitiesNode = rootNode.path("entities");

            if (entitiesNode.isMissingNode() || !entitiesNode.isArray()) {
                model.addAttribute("message", "Invalid JSON format: 'entities' array is missing or invalid.");
                return "error";
            }

            processEntities(projectName, entitiesNode, model);
            model.addAttribute("message", successMessage);
            return "result";
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred while processing JSON: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    private void processEntities(String projectName, JsonNode entitiesNode, Model model) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        for (JsonNode entityNode : entitiesNode) {
            String entityName = entityNode.path("name").asText();
            JsonNode attributesNode = entityNode.path("attributes");

            if (attributesNode.isMissingNode() || !attributesNode.isArray()) {
                model.addAttribute("message", "Invalid JSON format: 'attributes' array is missing or invalid for entity: " + entityName);
                return;
            }

            projectEntitiesService.generateEntityClass(projectName, entityName);
            projectEntitiesService.generateRepositoryClass(projectName, entityName);

            for (JsonNode attributeNode : attributesNode) {
                AttributeDTO attribute = objectMapper.treeToValue(attributeNode, AttributeDTO.class);
                projectAttributesService.addAttributesToEntity(projectName, entityName, attribute);
            }

            projectEntitiesService.generateServiceClass(projectName, entityName, projectAttributesService.getAttributes());
            projectEntitiesService.generateControllerClass(projectName, entityName);

            projectAttributesService.clearAttributes();
        }

        projectEntitiesService.generateSecurityClass(projectName);
        projectEntitiesService.generateDataInitializerClass(projectName);

        projectEntitiesService.generateAuthenticationServiceClass(projectName);
        projectEntitiesService.generateAuthenticationControllerClass(projectName);

        projectEntitiesService.generateUtils(projectName);
    }
}