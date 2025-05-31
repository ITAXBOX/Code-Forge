package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.utils.EntityContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EntityJsonProcessorService {

    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final FrontendService frontendService;
    private final AttributeStorageService attributeStorageService;
    private final BehaviorService behaviorService;
    private final EntityContext entityContext;

    public String processJsonAndGenerateEntities(boolean AI, String projectName, String json, Model model, String frontend, String backend, String database, String successMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode entitiesNode = rootNode.path("entities");

            if (entitiesNode.isMissingNode() || !entitiesNode.isArray()) {
                model.addAttribute("message", "Invalid JSON format: 'entities' array is missing or invalid.");
                return "error";
            }

            List<String> entityNames = new ArrayList<>();
            for (JsonNode entityNode : entitiesNode) {
                String entityName = entityNode.path("name").asText();
                entityNames.add(entityName);
            }

            model.addAttribute("projectName", projectName);
            model.addAttribute("frontendType", frontend);
            model.addAttribute("backendType", backend);
            model.addAttribute("databaseType", database);
            model.addAttribute("entityNames", entityNames);

            processEntities(AI, projectName, entitiesNode, model);
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

    private void processEntities(boolean AI, String projectName, JsonNode entitiesNode, Model model) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        for (JsonNode entityNode : entitiesNode) {
            String entityName = entityNode.path("name").asText();
            JsonNode attributesNode = entityNode.path("attributes");

            if (attributesNode.isMissingNode() || !attributesNode.isArray()) {
                model.addAttribute("message", "Invalid JSON format: 'attributes' array is missing or invalid for entity: " + entityName);
                return;
            }

            entityCodeGeneratorService.generateEntityClass(projectName, entityName);
            if (!AI) entityCodeGeneratorService.generateRepositoryClass(projectName, entityName);

            for (JsonNode attributeNode : attributesNode) {
                AttributeDTO attribute = objectMapper.treeToValue(attributeNode, AttributeDTO.class);
                attributeStorageService.addAttributesToEntity(projectName, entityName, attribute);
            }

            entityCodeGeneratorService.generateServiceClass(projectName, entityName, attributeStorageService.getAttributes());
            entityCodeGeneratorService.generateControllerClass(projectName, entityName);

            if (AI) {
                List<AttributeDTO> attributes = new ArrayList<>();
                for (JsonNode attributeNode : attributesNode) {
                    AttributeDTO attribute = objectMapper.treeToValue(attributeNode, AttributeDTO.class);
                    attributes.add(attribute);
                }
                entityContext.addEntity(entityName, attributes);
            }

            attributeStorageService.clearAttributes();
        }

        if (AI) {
            for (JsonNode entityNode : entitiesNode) {
                String entityName = entityNode.path("name").asText();
                JsonNode attributesNode = entityNode.path("attributes");

                List<AttributeDTO> attributes = new ArrayList<>();
                for (JsonNode attributeNode : attributesNode) {
                    attributes.add(objectMapper.treeToValue(attributeNode, AttributeDTO.class));
                }

                behaviorService.generateEntityServiceBehaviors(
                        projectName,
                        entityName,
                        attributes
                );
            }
        }

        entityCodeGeneratorService.generateSecurityClass(projectName);
        entityCodeGeneratorService.generateDataInitializerClass(projectName);

        entityCodeGeneratorService.generateAuthenticationServiceClass(projectName);
        entityCodeGeneratorService.generateAuthenticationControllerClass(projectName);

        entityCodeGeneratorService.generateUtils(projectName);

        if (AI)
            frontendService.createFrontendFiles(projectName);
    }
}