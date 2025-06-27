package DrMuhamadMubarak.TheFuture.frontend.nextjs.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EntityInfo;
import DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata.EndpointInfo;

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
                    // Extract attributes from entity class
                    enrichEntityWithAttributes(projectDir, entityInfo);
                    
                    // Extract behavior controller endpoints
                    extractBehaviorControllerEndpoints(projectDir, entityInfo);
                    
                    entities.add(entityInfo);
                }
            }

            return entities;
        } catch (Exception e) {
            System.err.println("Error extracting entities: " + e.getMessage());
            e.printStackTrace();
            return entities;
        }
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

    /**
     * Extracts behavior controller endpoints and adds them to the entity info
     */
    private void extractBehaviorControllerEndpoints(File projectDir, EntityInfo entityInfo) {
        try {
            File behaviorControllersDir = new File(projectDir, "behaviorcontrollers");
            if (!behaviorControllersDir.exists()) return;

            File behaviorControllerFile = new File(behaviorControllersDir, entityInfo.getName() + "BehaviorController.java");
            if (!behaviorControllerFile.exists()) return;

            try (BufferedReader reader = new BufferedReader(new FileReader(behaviorControllerFile))) {
                String line;
                String baseEndpoint = "/api/" + entityInfo.getName().toLowerCase() + "s";
                String currentMethod = null;
                String currentPath = null;
                String currentReturnType = null;
                String currentParams = null;
                String currentHttpMethod = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // Look for mapping annotations
                    if (line.startsWith("@GetMapping") || line.startsWith("@PostMapping") || 
                        line.startsWith("@PutMapping") || line.startsWith("@DeleteMapping")) {
                        
                        // Extract path from annotation
                        Pattern pathPattern = Pattern.compile("@\\w+Mapping\\(\"([^\"]*)\"\\)");
                        Matcher pathMatcher = pathPattern.matcher(line);
                        if (pathMatcher.find()) {
                            currentPath = pathMatcher.group(1);
                            
                            // Determine HTTP method from the annotation
                            if (line.startsWith("@GetMapping")) currentHttpMethod = "GET";
                            else if (line.startsWith("@PostMapping")) currentHttpMethod = "POST";
                            else if (line.startsWith("@PutMapping")) currentHttpMethod = "PUT";
                            else if (line.startsWith("@DeleteMapping")) currentHttpMethod = "DELETE";
                        }
                        continue;
                    }
                    
                    // Look for method signature
                    if (currentPath != null && currentHttpMethod != null && line.startsWith("public ")) {
                        Pattern methodPattern = Pattern.compile("public\\s+([\\w<>,\\s]+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
                        Matcher methodMatcher = methodPattern.matcher(line);
                        if (methodMatcher.find()) {
                            currentReturnType = methodMatcher.group(1).trim();
                            currentMethod = methodMatcher.group(2).trim();
                            currentParams = methodMatcher.group(3).trim();
                            
                            // Create endpoint info
                            EndpointInfo endpointInfo = new EndpointInfo(currentHttpMethod, baseEndpoint + currentPath, true);
                            
                            // Generate meaningful description based on method name and path
                            String description = generateEndpointDescription(currentMethod, currentPath, currentHttpMethod);
                            endpointInfo.setDescription(description);
                            
                            // Extract parameters with proper type information
                            extractParametersFromSignature(currentParams, endpointInfo);
                            
                            entityInfo.addBehaviorEndpoint(endpointInfo);
                            
                            // Reset for next endpoint
                            currentPath = null;
                            currentMethod = null;
                            currentReturnType = null;
                            currentParams = null;
                            currentHttpMethod = null;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading behavior controller file: " + e.getMessage());
        }
    }

    /**
     * Generates a meaningful description for the endpoint based on method name and path
     */
    private String generateEndpointDescription(String methodName, String path, String httpMethod) {
        StringBuilder description = new StringBuilder();
        
        // Add method name as the main action
        description.append(methodName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase());
        
        // Add context based on path
        if (path.contains("search")) {
            description.append(" - Search for specific entities based on criteria");
        } else if (path.contains("featured")) {
            description.append(" - Get featured or highlighted entities");
        } else if (path.contains("process")) {
            description.append(" - Process or execute an action on the entity");
        } else if (path.contains("history")) {
            description.append(" - Get historical data or activity log");
        } else if (path.contains("verify")) {
            description.append(" - Verify or validate the entity");
        } else if (path.contains("refund")) {
            description.append(" - Process a refund or reversal");
        } else if (path.contains("discount")) {
            description.append(" - Apply a discount or special pricing");
        } else if (path.contains("low-stock")) {
            description.append(" - Get items with low inventory levels");
        } else if (path.contains("count")) {
            description.append(" - Count or aggregate data");
        } else if (path.contains("by")) {
            description.append(" - Filter entities by specific criteria");
        } else if (path.contains("add")) {
            description.append(" - Add or associate data");
        } else if (path.contains("remove")) {
            description.append(" - Remove or disassociate data");
        } else if (path.contains("update")) {
            description.append(" - Update specific fields");
        } else if (path.contains("create")) {
            description.append(" - Create new entity with custom logic");
        } else if (path.contains("delete")) {
            description.append(" - Delete with custom logic");
        } else {
            description.append(" - Custom behavior operation");
        }
        
        return description.toString();
    }

    /**
     * Extracts parameter information from method signature
     */
    private void extractParametersFromSignature(String params, EndpointInfo endpointInfo) {
        if (params == null || params.trim().isEmpty()) return;

        // Split parameters by comma, handling generics
        List<String> paramList = splitParameters(params);
        
        for (String param : paramList) {
            param = param.trim();
            if (param.isEmpty()) continue;

            // Parse parameter: @PathVariable/@RequestParam type name
            Pattern paramPattern = Pattern.compile("(@PathVariable|@RequestParam)?\\s*([\\w<>,\\s]+)\\s+(\\w+)");
            Matcher matcher = paramPattern.matcher(param);
            
            if (matcher.find()) {
                String annotation = matcher.group(1);
                String type = matcher.group(2).trim();
                String name = matcher.group(3).trim();
                
                boolean required = annotation == null || !annotation.contains("required=false");
                String tsType = mapJavaTypeToTypeScript(type);
                
                // Generate parameter description based on name and type
                String description = generateParameterDescription(name, type, annotation);
                
                // Create enhanced parameter info
                EndpointInfo.ParameterInfo paramInfo = new EndpointInfo.ParameterInfo(name, tsType, required, description, type);
                endpointInfo.addParameter(paramInfo);
            }
        }
    }

    /**
     * Generates a description for a parameter based on its name and type
     */
    private String generateParameterDescription(String paramName, String javaType, String annotation) {
        StringBuilder description = new StringBuilder();
        
        // Add parameter type context
        if (annotation != null) {
            if (annotation.contains("@PathVariable")) {
                description.append("Path variable - ");
            } else if (annotation.contains("@RequestParam")) {
                description.append("Query parameter - ");
            } else {
                description.append("Parameter - ");
            }
        }
        
        // Add description based on parameter name
        if (paramName.equalsIgnoreCase("id")) {
            description.append("Entity identifier");
        } else if (paramName.equalsIgnoreCase("name")) {
            description.append("Entity name");
        } else if (paramName.equalsIgnoreCase("query") || paramName.equalsIgnoreCase("search")) {
            description.append("Search query term");
        } else if (paramName.equalsIgnoreCase("limit")) {
            description.append("Maximum number of results");
        } else if (paramName.equalsIgnoreCase("offset") || paramName.equalsIgnoreCase("page")) {
            description.append("Pagination offset");
        } else if (paramName.equalsIgnoreCase("status")) {
            description.append("Status value");
        } else if (paramName.equalsIgnoreCase("date") || paramName.equalsIgnoreCase("startDate") || paramName.equalsIgnoreCase("endDate")) {
            description.append("Date value");
        } else if (paramName.equalsIgnoreCase("amount") || paramName.equalsIgnoreCase("price") || paramName.equalsIgnoreCase("discount")) {
            description.append("Numeric value");
        } else if (paramName.equalsIgnoreCase("email")) {
            description.append("Email address");
        } else if (paramName.equalsIgnoreCase("username")) {
            description.append("Username");
        } else if (paramName.equalsIgnoreCase("password")) {
            description.append("Password");
        } else if (paramName.equalsIgnoreCase("category") || paramName.equalsIgnoreCase("categoryId")) {
            description.append("Category identifier");
        } else if (paramName.equalsIgnoreCase("product") || paramName.equalsIgnoreCase("productId")) {
            description.append("Product identifier");
        } else if (paramName.equalsIgnoreCase("user") || paramName.equalsIgnoreCase("userId")) {
            description.append("User identifier");
        } else if (paramName.equalsIgnoreCase("promotion") || paramName.equalsIgnoreCase("promotionId")) {
            description.append("Promotion identifier");
        } else if (paramName.equalsIgnoreCase("review") || paramName.equalsIgnoreCase("reviewId")) {
            description.append("Review identifier");
        } else if (paramName.equalsIgnoreCase("role") || paramName.equalsIgnoreCase("roleId")) {
            description.append("Role identifier");
        } else if (paramName.equalsIgnoreCase("inventory") || paramName.equalsIgnoreCase("inventoryId")) {
            description.append("Inventory identifier");
        } else {
            // Generic description based on type
            if (javaType.contains("String")) {
                description.append("Text value");
            } else if (javaType.contains("Integer") || javaType.contains("Long") || javaType.contains("int") || javaType.contains("long")) {
                description.append("Integer value");
            } else if (javaType.contains("Double") || javaType.contains("Float") || javaType.contains("double") || javaType.contains("float")) {
                description.append("Decimal value");
            } else if (javaType.contains("Boolean") || javaType.contains("boolean")) {
                description.append("Boolean value");
            } else if (javaType.contains("Date") || javaType.contains("LocalDate") || javaType.contains("LocalDateTime")) {
                description.append("Date/time value");
            } else {
                description.append("Data value");
            }
        }
        
        return description.toString();
    }

    /**
     * Splits parameters by comma, handling generics
     */
    private List<String> splitParameters(String params) {
        List<String> result = new ArrayList<>();
        if (params == null || params.trim().isEmpty()) {
            return result;
        }

        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : params.toCharArray()) {
            if (c == '<') depth++;
            if (c == '>') depth--;

            if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }
}
