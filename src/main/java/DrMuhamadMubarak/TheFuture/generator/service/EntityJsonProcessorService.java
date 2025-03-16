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
public class EntityJsonProcessorService {

    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final AttributeStorageService attributeStorageService;

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

            entityCodeGeneratorService.generateEntityClass(projectName, entityName);
            entityCodeGeneratorService.generateRepositoryClass(projectName, entityName);

            for (JsonNode attributeNode : attributesNode) {
                AttributeDTO attribute = objectMapper.treeToValue(attributeNode, AttributeDTO.class);
                attributeStorageService.addAttributesToEntity(projectName, entityName, attribute);
            }

            entityCodeGeneratorService.generateServiceClass(projectName, entityName, attributeStorageService.getAttributes());
            entityCodeGeneratorService.generateControllerClass(projectName, entityName);

            attributeStorageService.clearAttributes();
        }

        entityCodeGeneratorService.generateSecurityClass(projectName);
        entityCodeGeneratorService.generateDataInitializerClass(projectName);

        entityCodeGeneratorService.generateAuthenticationServiceClass(projectName);
        entityCodeGeneratorService.generateAuthenticationControllerClass(projectName);

        entityCodeGeneratorService.generateUtils(projectName);
    }
}