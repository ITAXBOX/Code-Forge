package DrMuhamadMubarak.TheFuture.frontend.nextjs.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EntityInfo;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EndpointInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for file operations and dashboard generation
 */
public class NextjsFileService {

    /**
     * Copies the template structure to the output directory
     */
    public void copyTemplateStructure(String sourceDir, String destDir) throws IOException {
        File sourceDirFile = new File(sourceDir);
        if (!sourceDirFile.exists()) {
            throw new IOException("Template directory not found: " + sourceDir);
        }

        // List of files/dirs to exclude
        List<String> excludeList = List.of("node_modules", ".next", ".idea", ".git", ".DS_Store");

        // Copy directory structure recursively
        copyDirectory(sourceDirFile, new File(destDir), excludeList);
    }

    /**
     * Recursively copies a directory
     */
    private void copyDirectory(File source, File destination, List<String> excludeList) throws IOException {
        if (excludeList.contains(source.getName())) {
            return;
        }

        if (source.isDirectory()) {
            // Create destination directory if it doesn't exist
            if (!destination.exists() && !destination.mkdirs()) {
                throw new IOException("Failed to create directory: " + destination);
            }

            String[] files = source.list();
            if (files == null) return;

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyDirectory(srcFile, destFile, excludeList);
            }
        } else {
            // Skip if file is dashboard.tsx as we'll generate it separately
            if (source.getName().equals("dashboard.tsx")) {
                return;
            }

            // Ensure parent directory exists
            File parentDir = destination.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }

            // Copy file
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Generates the customized dashboard file
     */
    public void generateDashboard(String projectName, String templateDir, String outputDir, List<EntityInfo> entities) throws IOException {
        // Read dashboard template
        String dashboardTemplate = new String(Files.readAllBytes(Paths.get(templateDir, "dashboard.tsx")));

        // Clean entities to only include base entity names without behavior entries
        List<EntityInfo> cleanEntities = cleanDuplicateEntities(entities);

        // Replace mock entities with actual entities from backend
        String entitiesCode = generateEntitiesCode(cleanEntities);
        dashboardTemplate = dashboardTemplate.replaceAll(
                "// Mock entities data\\s+const entities = \\[(.|\\n)*?];",
            "// Entities data from backend\n  const entities = " + entitiesCode + ";"
        );

        // Replace project name
        dashboardTemplate = dashboardTemplate.replace("E-Commerce API", projectName + " API");
        dashboardTemplate = dashboardTemplate.replace("Spring Boot / Java", projectName + " / Spring Boot");

        // Replace "ENTITY MANAGER" with the project name
        dashboardTemplate = dashboardTemplate.replace("ENTITY MANAGER", projectName.toUpperCase() + " DASHBOARD");

        // Replace description text under the title - using regex with Pattern.DOT ALL to match across multiple lines
        dashboardTemplate = dashboardTemplate.replaceAll(
            "(?s)<p className=\"text-gray-600 max-w-md mx-auto\">\\s*Generate complete backend applications with AI, including entity\\s*definitions, CRUD endpoints, and custom behavior\\.\\s*</p>",
            "<p className=\"text-gray-600 max-w-md mx-auto\">The Art of CodeForge is Crafted Here</p>"
        );

        // Write the generated dashboard file
        Path dashboardPath = Paths.get(outputDir, "dashboard.tsx");
        Files.write(dashboardPath, dashboardTemplate.getBytes());
    }

    /**
     * Updates the package.json file with project info
     */
    public void updatePackageJson(String projectName, String outputDir) throws IOException {
        Path packageJsonPath = Paths.get(outputDir, "package.json");
        String packageJson = new String(Files.readAllBytes(packageJsonPath));

        // Replace name in package.json
        packageJson = packageJson.replaceFirst(
            "\"name\": \".*\"",
            "\"name\": \"" + projectName.toLowerCase().replace(" ", "-") + "-dashboard\""
        );

        // Add axios dependency for API calls
        packageJson = packageJson.replaceFirst(
            "\"dependencies\": \\{",
            "\"dependencies\": {\n    \"axios\": \"^1.6.0\","
        );

        // Write updated package.json
        Files.write(packageJsonPath, packageJson.getBytes());
    }

    /**
     * Generates the code representation of entities for the dashboard
     */
    private String generateEntitiesCode(List<EntityInfo> entities) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < entities.size(); i++) {
            EntityInfo entity = entities.get(i);
            sb.append("    {\n");
            sb.append("      name: \"").append(entity.getName()).append("\",\n");
            sb.append("      endpoints: [");

            List<String> formattedEndpoints = new ArrayList<>();
            for (EndpointInfo endpoint : entity.getEndpoints()) {
                String formatted = "\"" + endpoint.getMethod();
                if (!endpoint.getPath().isEmpty()) {
                    formatted += " " + endpoint.getPath();
                }
                formatted += "\"";
                formattedEndpoints.add(formatted);
            }

            sb.append(String.join(", ", formattedEndpoints));
            sb.append("],\n");
            sb.append("    }");

            if (i < entities.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }

        sb.append("  ]");
        return sb.toString();
    }

    /**
     * Cleans the entity list to remove duplicate entity names with behavior
     * This ensures only base entity names appear in the sidebar
     */
    private List<EntityInfo> cleanDuplicateEntities(List<EntityInfo> entities) {
        // Create a map to store unique entities by name
        java.util.Map<String, EntityInfo> uniqueEntities = new java.util.LinkedHashMap<>();

        // Process entities to keep only the base entities
        for (EntityInfo entity : entities) {
            String entityName = entity.getName();

            // Skip entities with "Behavior" suffix
            if (entityName.endsWith("Behavior")) {
                continue;
            }

            // Skip entities with slashes or spaces (typically custom endpoints)
            if (entityName.contains("/") || entityName.contains(" ")) {
                continue;
            }

            // Only add if not already in the map
            if (!uniqueEntities.containsKey(entityName)) {
                uniqueEntities.put(entityName, entity);
            }
        }

        // Return the list of unique entities
        return new ArrayList<>(uniqueEntities.values());
    }
}
