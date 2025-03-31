package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.ai.AIService;
import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.generator.dto.BehaviorGenerationResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static DrMuhamadMubarak.TheFuture.generator.ai.ThePrompt.BEHAVIOR_PROMPT;

@Service
@AllArgsConstructor
public class BehaviorService {
    private final AIService aiService;
    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final BehaviorControllerService behaviorControllerService;

    public void generateEntityServiceBehaviors(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String attributeDescription = createAttributeDescription(attributes);
        String repositories = getRequiredRepositories(entityName, attributes);

        String prompt = String.format(BEHAVIOR_PROMPT,
                entityName,
                attributeDescription,
                repositories,
                attributeDescription,
                entityName);

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

        behaviorControllerService.generateControllerBehaviors(projectName, entityName, validatedMethods);
    }

    private String createAttributeDescription(List<AttributeDTO> attributes) {
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(attr -> attr.getAttributeName() + ":" + attr.getDataType())
                .collect(Collectors.joining(", "));
    }

    private String getRequiredRepositories(String entityName, List<AttributeDTO> attributes) {
        return Stream.concat(
                Stream.of(entityName + "Repository"),
                attributes.stream()
                        .filter(attr -> attr.getRelationshipType() != null)
                        .map(attr -> attr.getRelatedEntity() + "Repository")
        ).distinct().collect(Collectors.joining(", "));
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