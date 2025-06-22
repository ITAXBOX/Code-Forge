package DrMuhamadMubarak.TheFuture.frontend.nextjs.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EntityInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for extracting entity and endpoint information from Spring controllers
 */
public class EntityExtractorService {

    /**
     * Gets entities from the backend by analyzing Spring controller files
     */
    public List<EntityInfo> getEntitiesFromBackend(String projectName) {
        List<EntityInfo> entities = new ArrayList<>();

        try {
            // Find controller files in the project
            File projectDir = new File(projectName + "/src/main/java/com/example/" + projectName.toLowerCase());
            List<File> controllerFiles = findControllerFiles(projectDir);

            // Process each controller file to extract entity information
            for (File controllerFile : controllerFiles) {
                EntityInfo entityInfo = extractEntityInfoFromController(controllerFile);
                if (entityInfo != null) {
                    entities.add(entityInfo);
                }
            }

            // Find and process entity class files to extract attributes
            for (EntityInfo entityInfo : entities) {
                enrichEntityWithAttributes(projectDir, entityInfo);

                // Special handling for User entity
                if (entityInfo.getName().equals("User")) {
                    System.out.println("Special handling for User entity with attributes: " + entityInfo.getAttributes());
                    // Make sure User entity has basic attributes if they weren't detected
                    if (entityInfo.getAttributes().isEmpty()) {
                        entityInfo.addAttribute("id", "number");
                        entityInfo.addAttribute("username", "string");
                        entityInfo.addAttribute("email", "string");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error extracting entity information: " + e.getMessage());
            e.printStackTrace();
        }

        return entities;
    }

    /**
     * Enriches the entity info with attribute information from entity class files
     */
    private void enrichEntityWithAttributes(File projectDir, EntityInfo entityInfo) {
        try {
            // Find the entity class file
            File entityFile = findEntityFile(projectDir, entityInfo.getName());
            if (entityFile != null) {
                extractAttributesFromEntityClass(entityFile, entityInfo);
            } else {
                System.out.println("Entity class file not found for: " + entityInfo.getName() + ", searching in models directory");
                // Try to find in models directory
                File modelsDir = new File(projectDir.getParentFile().getParentFile().getParentFile(), "models");
                if (modelsDir.exists()) {
                    entityFile = findEntityFile(modelsDir, entityInfo.getName());
                    if (entityFile != null) {
                        extractAttributesFromEntityClass(entityFile, entityInfo);
                        return;
                    }
                }

                System.out.println("Entity class file not found, adding default attributes for: " + entityInfo.getName());
                // Add default attributes if we can't find the entity class
                entityInfo.addAttribute("id", "number");
                entityInfo.addAttribute("name", "string");
                entityInfo.addAttribute("description", "string");
            }
        } catch (Exception e) {
            System.err.println("Error enriching entity with attributes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds the entity class file for a given entity name
     */
    private File findEntityFile(File directory, String entityName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File result = findEntityFile(file, entityName);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(entityName + ".java")) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Extracts attribute information from entity class file
     */
    private void extractAttributesFromEntityClass(File entityFile, EntityInfo entityInfo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(entityFile))) {
            boolean inClassBody = false;
            String line;

            // Pattern to match attribute declarations in the entity class
            Pattern attributePattern = Pattern.compile("\\s*(private|protected|public)?\\s+(\\w+)\\s+(\\w+)(\\s*=.*)?;");
            // Enhanced pattern to catch Lombok annotated fields
            Pattern lombokFieldPattern = Pattern.compile("\\s*@Column.*\\s*private\\s+(\\w+)\\s+(\\w+)\\s*;");

            while ((line = reader.readLine()) != null) {
                // Check if we've entered the class body
                if (line.contains("class " + entityInfo.getName())) {
                    inClassBody = true;
                    continue;
                }

                // Skip lines until we're inside the class body
                if (!inClassBody) continue;

                // Stop if we've reached the end of the class
                if (line.contains("}") && line.trim().equals("}")) break;

                // Check for Lombok style fields first
                Matcher lombokMatcher = lombokFieldPattern.matcher(line);
                if (lombokMatcher.find()) {
                    String type = lombokMatcher.group(1);
                    String name = lombokMatcher.group(2);

                    // Map Java types to TypeScript types for frontend
                    String tsType = mapJavaTypeToTypeScript(type);
                    entityInfo.addAttribute(name, tsType);
                    continue;
                }

                // Extract attribute information
                Matcher attributeMatcher = attributePattern.matcher(line);
                if (attributeMatcher.find()) {
                    String type = attributeMatcher.group(2);
                    String name = attributeMatcher.group(3);

                    // Skip static or final fields, typically not entity attributes
                    if (line.contains("static") || line.contains("final")) continue;

                    // Map Java types to TypeScript types for frontend
                    String tsType = mapJavaTypeToTypeScript(type);

                    entityInfo.addAttribute(name, tsType);
                }
            }

            // If no attributes were found, add default ones
            if (entityInfo.getAttributes().isEmpty()) {
                System.out.println("No attributes found for entity: " + entityInfo.getName() + " in file: " + entityFile.getAbsolutePath());
                entityInfo.addAttribute("id", "number");
                entityInfo.addAttribute("name", "string");

                // Special handling for User entity
                if (entityInfo.getName().equals("User")) {
                    entityInfo.addAttribute("username", "string");
                    entityInfo.addAttribute("email", "string");
                } else {
                    entityInfo.addAttribute("description", "string");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading entity file: " + e.getMessage());
        }
    }

    /**
     * Maps Java types to TypeScript types
     */
    private String mapJavaTypeToTypeScript(String javaType) {
        return switch (javaType) {
            case "int", "Integer", "long", "Long", "double", "Double", "float", "Float" -> "number";
            case "boolean", "Boolean" -> "boolean";
            case "Date", "LocalDate", "LocalDateTime", "ZonedDateTime" -> "Date";
            default -> "string";
        };
    }

    private List<File> findControllerFiles(File directory) {
        List<File> controllerFiles = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        controllerFiles.addAll(findControllerFiles(file));
                    } else if (file.getName().endsWith("Controller.java")) {
                        controllerFiles.add(file);
                    }
                }
            }
        }
        return controllerFiles;
    }

    private EntityInfo extractEntityInfoFromController(File controllerFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(controllerFile))) {
            String entityName = extractEntityNameFromController(controllerFile.getName());
            if (entityName == null) return null;

            EntityInfo entityInfo = new EntityInfo(entityName);

            // Patterns to match endpoint annotations
            Pattern getMapping = Pattern.compile("@GetMapping\\(\"([^\"]*)\"\\)");
            Pattern postMapping = Pattern.compile("@PostMapping\\(\"([^\"]*)\"\\)");
            Pattern putMapping = Pattern.compile("@PutMapping\\(\"([^\"]*)\"\\)");
            Pattern deleteMapping = Pattern.compile("@DeleteMapping\\(\"([^\"]*)\"\\)");

            String line;
            String baseEndpoint = "/api/" + entityName.toLowerCase() + "s";
            entityInfo.setBaseEndpoint(baseEndpoint);

            // Add standard CRUD endpoints
            entityInfo.addEndpoint("GET", baseEndpoint);
            entityInfo.addEndpoint("POST", baseEndpoint);
            entityInfo.addEndpoint("PUT", baseEndpoint + "/{id}");
            entityInfo.addEndpoint("DELETE", baseEndpoint + "/{id}");

            // Look for custom behavior endpoints
            while ((line = reader.readLine()) != null) {
                Matcher getMatcher = getMapping.matcher(line);
                if (getMatcher.find()) {
                    String path = getMatcher.group(1);
                    entityInfo.addEndpoint("GET", baseEndpoint + path, true);
                    continue;
                }

                Matcher postMatcher = postMapping.matcher(line);
                if (postMatcher.find()) {
                    String path = postMatcher.group(1);
                    if (!path.isEmpty()) { // Skip if it's the default POST endpoint
                        entityInfo.addEndpoint("POST", baseEndpoint + path, true);
                    }
                    continue;
                }

                Matcher putMatcher = putMapping.matcher(line);
                if (putMatcher.find()) {
                    String path = putMatcher.group(1);
                    if (!path.equals("/{id}")) { // Skip if it's the default PUT endpoint
                        entityInfo.addEndpoint("PUT", baseEndpoint + path, true);
                    }
                    continue;
                }

                Matcher deleteMatcher = deleteMapping.matcher(line);
                if (deleteMatcher.find()) {
                    String path = deleteMatcher.group(1);
                    if (!path.equals("/{id}")) { // Skip if it's the default DELETE endpoint
                        entityInfo.addEndpoint("DELETE", baseEndpoint + path, true);
                    }
                }
            }

            return entityInfo;
        } catch (IOException e) {
            System.err.println("Error reading controller file: " + e.getMessage());
            return null;
        }
    }

    private String extractEntityNameFromController(String fileName) {
        // Extract entity name from Controller file name
        // UserController.java -> User
        if (fileName.contains("Controller.java")) {
            return fileName.substring(0, fileName.indexOf("Controller.java"));
        }
        return null;
    }
}
