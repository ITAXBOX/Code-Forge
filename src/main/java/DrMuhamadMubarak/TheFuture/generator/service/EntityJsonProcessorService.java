package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.codeforge.builder.ProjectCreateRequestBuilder;
import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectCreateRequestDTO;
import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectService;
import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.utils.EntityContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class EntityJsonProcessorService {

    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final FrontendService frontendService;
    private final AttributeStorageService attributeStorageService;
    private final BehaviorService behaviorService;
    private final EntityContext entityContext;
    private final ProjectService projectService;

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
            if (AI)
                model.addAttribute("fromJsonProcessor", true);
            processEntities(AI, projectName, entitiesNode, model);
            model.addAttribute("message", successMessage);
            if (AI) {
                ProjectCreateRequestDTO dto = ProjectCreateRequestBuilder.ProjectCreateRequestDTOBuilder(projectName,null, frontend, backend, database);
                projectService.createProject(dto);
                copyProjectToProjectsDirectory(projectName);
            }
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

            // Remove empty lines between braces in the model file
            removeEmptyLinesInModelFile(projectName, entityName);

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

    private void removeEmptyLinesInModelFile(String projectName, String entityName) {
        try {
            String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models";
            String className = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
            String filePath = baseDir + "/" + className + ".java";

            // Read the file content
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            List<String> processedLines = new ArrayList<>();

            boolean insideClassBody = false;

            for (String line : lines) {
                String trimmedLine = line.trim();

                // Check if we're entering the class body
                if (trimmedLine.contains("public class") && trimmedLine.contains("{")) {
                    insideClassBody = true;
                    processedLines.add(line);
                    continue;
                }

                // Check if we're exiting the class body
                if (trimmedLine.equals("}") && insideClassBody) {
                    insideClassBody = false;
                    processedLines.add(line);
                    continue;
                }

                // Process lines inside class body
                if (insideClassBody) {
                    // Skip empty lines
                    if (trimmedLine.isEmpty()) {
                        continue;
                    }

                    // Add non-empty lines
                    processedLines.add(line);
                } else {
                    // Add lines outside class body as is
                    processedLines.add(line);
                }
            }

            // Write the processed content back to the file
            Files.write(path, String.join(System.lineSeparator(), processedLines).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyProjectToProjectsDirectory(String projectName) {
        try {
            Path sourcePath = Paths.get("./" + projectName);
            Path projectsDir = Paths.get("./Projects");
            Path targetPath = projectsDir.resolve(projectName);

            // Create Projects directory if it doesn't exist
            if (!Files.exists(projectsDir)) {
                Files.createDirectory(projectsDir);
            }

            // Delete the target directory if it already exists
            if (Files.exists(targetPath)) {
                deleteDirectory(targetPath);
            }

            // Copy the project directory to the Projects directory
            copyDirectory(sourcePath, targetPath);

            // Delete the original project directory after successful copy
            deleteDirectory(sourcePath);

            System.out.println("Project '" + projectName + "' successfully copied to Projects directory and original directory deleted");
        } catch (IOException e) {
            System.err.println("Failed to copy project to Projects directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    try {
                        Path targetPath = target.resolve(source.relativize(sourcePath));
                        if (Files.isDirectory(sourcePath)) {
                            if (!Files.exists(targetPath)) {
                                Files.createDirectory(targetPath);
                            }
                        } else {
                            Files.copy(sourcePath, targetPath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder()) // Sort in reverse order to delete files before directories
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}