package DrMuhamadMubarak.TheFuture.frontend.nextjs.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.model.EntityInfo;

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

        } catch (Exception e) {
            System.err.println("Error extracting entity information: " + e.getMessage());
        }

        return entities;
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
