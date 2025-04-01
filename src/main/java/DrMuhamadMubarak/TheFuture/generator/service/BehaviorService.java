package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.ai.AIService;
import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.generator.dto.BehaviorGenerationResult;
import DrMuhamadMubarak.TheFuture.utils.EntityContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static DrMuhamadMubarak.TheFuture.generator.ai.ThePrompt.BEHAVIOR_PROMPT;

@Service
@AllArgsConstructor
public class BehaviorService {
    private final AIService aiService;
    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final BehaviorControllerService behaviorControllerService;
    private final EntityContext entityContext;

    public void generateEntityServiceBehaviors(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String attributeDescription = createAttributeDescription(attributes);
        String repositoriesWithAttributes = buildRepositoriesWithAttributes(entityName, attributes);
        String relatedEntitiesAttributes = getRelatedEntitiesAttributes(attributes);

        String prompt = String.format(BEHAVIOR_PROMPT,
                entityName,
                attributeDescription,
                repositoriesWithAttributes,
                attributeDescription,
                relatedEntitiesAttributes,
                entityName
        );

        BehaviorGenerationResult result = aiService.generateBehaviorCode(prompt)
                .blockOptional()
                .orElseGet(() -> new BehaviorGenerationResult(""));

        String validatedMethods = processGeneratedMethods(
                result.getMethods(),
                entityName,
                attributes
        );

        entityCodeGeneratorService.generateBehaviorServiceClass(
                projectName,
                entityName,
                validatedMethods
        );

        behaviorControllerService.generateControllerBehaviors(
                projectName,
                entityName,
                validatedMethods);
    }

    private String buildRepositoriesWithAttributes(String entityName, List<AttributeDTO> attributes) {
        StringBuilder sb = new StringBuilder();

        // Add main entity repository with attributes
        sb.append("- ").append(entityName).append("Repository: ")
                .append(createAttributeDescription(attributes)).append("\n");

        // Add related entity repositories with their attributes
        attributes.stream()
                .filter(attr -> attr.getRelationshipType() != null)
                .forEach(attr -> {
                    String relatedEntity = attr.getRelatedEntity();
                    if (entityContext.hasEntity(relatedEntity)) {
                        List<AttributeDTO> relatedAttrs = entityContext.getAttributesFor(relatedEntity);
                        sb.append("- ").append(relatedEntity).append("Repository: ")
                                .append(createAttributeDescription(relatedAttrs)).append("\n");
                    }
                });

        return sb.toString();
    }

    private String getRelatedEntitiesAttributes(List<AttributeDTO> attributes) {
        String result = attributes.stream()
                .filter(attr -> attr.getRelationshipType() != null)
                .filter(attr -> entityContext.hasEntity(attr.getRelatedEntity()))
                .map(attr -> {
                    List<AttributeDTO> relatedAttrs = entityContext.getAttributesFor(attr.getRelatedEntity());
                    return attr.getRelatedEntity() + ": " + createAttributeDescription(relatedAttrs);
                })
                .collect(Collectors.joining(", "));

        return result.isEmpty() ? "NONE" : result;
    }

    // Modified to include relationship information
    private String createAttributeDescription(List<AttributeDTO> attributes) {
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(attr -> {
                    String desc = attr.getAttributeName() + ":" + attr.getDataType();
                    if (attr.getRelationshipType() != null) {
                        desc += "(" + attr.getRelationshipType() + " to " + attr.getRelatedEntity() + ")";
                    }
                    return desc;
                })
                .collect(Collectors.joining(", "));
    }

    private String processGeneratedMethods(String methods, String entityName,
                                           List<AttributeDTO> attributes) {
        // 1. Clean up the generated methods
        String cleaned = methods.replaceAll("//.*", "") // Remove comments
                .replaceAll("@Override", "") // Remove overrides
                .replaceAll("package .*?;", "") // Remove package
                .replaceAll("import .*?;", "") // Remove imports
                .replaceAll("\\.equals\\(", "=="); // Fix ID comparisons

        // 2. Extract all used repositories
        Set<String> usedRepos = new HashSet<>();
        Matcher matcher = Pattern.compile("(\\w+Repository)\\.\\w+\\(").matcher(cleaned);
        while (matcher.find()) {
            usedRepos.add(matcher.group(1));
        }

        // 3. Get all available repositories
        Set<String> availableRepos = attributes.stream()
                .filter(attr -> attr.getRelationshipType() != null)
                .map(attr -> attr.getRelatedEntity() + "Repository")
                .collect(Collectors.toSet());
        availableRepos.add(entityName + "Repository");

        // 4. Find missing repositories (used but not declared)
        Set<String> missingRepos = new HashSet<>(usedRepos);
        missingRepos.removeAll(availableRepos);

        if (!missingRepos.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("// Auto-injected repositories\n");
            missingRepos.forEach(repo -> {
                String fieldName = repo.substring(0, 1).toLowerCase() + repo.substring(1);
                sb.append("private final ").append(repo.substring(0, 1).toUpperCase()).append(repo.substring(1))
                        .append(" ")
                        .append(fieldName)
                        .append(";\n");
            });
            sb.append("\n").append(cleaned);
            return sb.toString();
        }

        return cleaned;
    }
}