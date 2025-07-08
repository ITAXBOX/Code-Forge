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
                    
                    // Add parameters for standard endpoints that are missing them
                    addStandardEndpointParameters(entityInfo);

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
        String currentHttpMethod = null;
        String currentPath = null;

        try {
            File behaviorControllersDir = new File(projectDir, "behaviorcontrollers");
            if (!behaviorControllersDir.exists()) {
                System.out.println("No behaviorcontrollers directory found for: " + entityInfo.getName());
                return;
            }

            File behaviorControllerFile = new File(behaviorControllersDir, entityInfo.getName() + "BehaviorController.java");
            if (!behaviorControllerFile.exists()) {
                System.out.println("No behavior controller file found for: " + entityInfo.getName());
                return;
            }

            System.out.println("Processing behavior controller: " + behaviorControllerFile.getAbsolutePath());

            try (BufferedReader reader = new BufferedReader(new FileReader(behaviorControllerFile))) {
                String line;
                String baseEndpoint = "/api/" + entityInfo.getName().toLowerCase() + "s";
                List<String> methodLines = new ArrayList<>();
                boolean inMethodSignature = false;

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

                            System.out.println("Found endpoint: " + currentHttpMethod + " " + currentPath);
                        }
                        continue;
                    }
                    
                    // Look for method signature after finding an annotation
                    if (currentPath != null && currentHttpMethod != null && line.startsWith("public ")) {
                        methodLines.clear();
                        methodLines.add(line);
                        inMethodSignature = true;

                        // Check if method signature is complete on this line
                        if (line.contains(")") && line.contains("{")) {
                            processCompleteMethodSignature(methodLines, currentHttpMethod, baseEndpoint.substring(0, baseEndpoint.length() - 1) + currentPath, entityInfo);
                            currentHttpMethod = null;
                            currentPath = null;
                        }
                        continue;
                    }

                    // Continue collecting method signature lines if we're in the middle of one
                    if (inMethodSignature) {
                        methodLines.add(line);

                        // Check if we've reached the end of the method signature
                        if (line.contains(")") && (line.contains("{") || line.trim().equals(")"))) {
                            processCompleteMethodSignature(methodLines, currentHttpMethod, baseEndpoint.substring(0, baseEndpoint.length() - 1) + currentPath, entityInfo);
                            currentHttpMethod = null;
                            currentPath = null;
                            inMethodSignature = false;
                        }
                    }
                }

                System.out.println("Found " + entityInfo.getBehaviorEndpoints().size() + " behavior endpoints for " + entityInfo.getName());
            }
        } catch (IOException e) {
            System.err.println("Error reading behavior controller file: " + e.getMessage());
        }
    }

    /**
     * Processes a complete method signature and creates endpoint info
     */
    private void processCompleteMethodSignature(List<String> methodLines, String httpMethod, String fullPath, EntityInfo entityInfo) {
        // Combine all method lines into a single string
        String completeSignature = String.join(" ", methodLines);

        // Extract method name and parameters
        Pattern methodPattern = Pattern.compile("public\\s+([\\w<>,\\s\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
        Matcher methodMatcher = methodPattern.matcher(completeSignature);

        if (methodMatcher.find()) {
            String methodName = methodMatcher.group(2).trim();
            String parameters = methodMatcher.group(3).trim();

            System.out.println("Processing method: " + methodName + " with params: " + parameters);

            // Create endpoint info
            EndpointInfo endpointInfo = new EndpointInfo(httpMethod, fullPath, true);

            // Generate meaningful description
            String description = generateEndpointDescription(methodName);
            endpointInfo.setDescription(description);

            // Extract parameters with proper type information
            extractParametersFromSignature(parameters, endpointInfo, fullPath);

            entityInfo.addBehaviorEndpoint(endpointInfo);

            System.out.println("Added behavior endpoint: " + httpMethod + " " + fullPath + " with " + endpointInfo.getParameters().size() + " parameters");
        } else {
            System.out.println("Could not parse method signature: " + completeSignature);
        }
    }

    /**
     * Generates a meaningful description for an endpoint based on method name and path
     */
    private String generateEndpointDescription(String methodName) {
        // Convert camelCase method name to readable description
        String description = methodName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

        // Add context based on method name patterns
        if (methodName.toLowerCase().contains("create")) {
            description += " - Create new entity with custom logic";
        } else if (methodName.toLowerCase().contains("update")) {
            description += " - Custom behavior operation";
        } else if (methodName.toLowerCase().contains("delete")) {
            description += " - Delete with custom logic";
        } else if (methodName.toLowerCase().contains("find") || methodName.toLowerCase().contains("get")) {
            description += " - Filter entities by specific criteria";
        } else if (methodName.toLowerCase().contains("count")) {
            description += " - Count or aggregate data";
        } else {
            description += " - Custom behavior operation";
        }

        return description;
    }

    /**
     * Extracts parameter information from method signature
     */
    private void extractParametersFromSignature(String parametersString, EndpointInfo endpointInfo, String fullPath) {
        if (parametersString.trim().isEmpty()) {
            return;
        }

        // Split parameters by comma, but be careful of generic types
        String[] paramParts = splitParameters(parametersString);

        for (String param : paramParts) {
            param = param.trim();
            if (param.isEmpty()) continue;

            // Parse parameter with annotations
            String paramType;
            String paramName;
            String javaType;
            boolean isRequired = true;
            String description;

            // Handle different parameter types
            if (param.contains("@PathVariable")) {
                // Extract path variable info
                Pattern pathVarPattern = Pattern.compile("@PathVariable(?:\\(\"([^\"]+)\"\\))?\\s+(\\w+)\\s+(\\w+)");
                Matcher pathVarMatcher = pathVarPattern.matcher(param);
                if (pathVarMatcher.find()) {
                    javaType = pathVarMatcher.group(2);
                    paramName = pathVarMatcher.group(3);
                    paramType = mapJavaTypeToTypeScript(javaType);
                    description = "Path variable - " + getTypeDescription(javaType, paramName);

                    endpointInfo.addParameter(new EndpointInfo.ParameterInfo(paramName, paramType, isRequired, description, javaType));
                }
            } else if (param.contains("@RequestParam")) {
                // Extract request parameter info
                Pattern reqParamPattern = Pattern.compile("@RequestParam(?:\\(\"([^\"]+)\"\\))?\\s+(\\w+)\\s+(\\w+)");
                Matcher reqParamMatcher = reqParamPattern.matcher(param);
                if (reqParamMatcher.find()) {
                    javaType = reqParamMatcher.group(2);
                    paramName = reqParamMatcher.group(3);
                    paramType = mapJavaTypeToTypeScript(javaType);
                    description = "Query parameter - " + getTypeDescription(javaType, paramName);

                    endpointInfo.addParameter(new EndpointInfo.ParameterInfo(paramName, paramType, isRequired, description, javaType));
                }
            } else if (param.contains("@RequestBody")) {
                // Extract request body info
                Pattern reqBodyPattern = Pattern.compile("@RequestBody\\s+(\\w+)\\s+(\\w+)");
                Matcher reqBodyMatcher = reqBodyPattern.matcher(param);
                if (reqBodyMatcher.find()) {
                    javaType = reqBodyMatcher.group(1);
                    paramName = reqBodyMatcher.group(2);

                    paramType = "object";
                    description = "Request body - Entity data";

                    endpointInfo.addParameter(new EndpointInfo.ParameterInfo(paramName, paramType, isRequired, description, javaType));
                }
            } else {
                // Handle parameters without explicit annotations (assume based on position and path)
                Pattern simpleParamPattern = Pattern.compile("(\\w+)\\s+(\\w+)");
                Matcher simpleParamMatcher = simpleParamPattern.matcher(param);
                if (simpleParamMatcher.find()) {
                    javaType = simpleParamMatcher.group(1);
                    paramName = simpleParamMatcher.group(2);

                    paramType = mapJavaTypeToTypeScript(javaType);

                    // Determine if it's a path variable by checking if the path contains the parameter
                    if (fullPath.contains("{" + paramName + "}")) {
                        description = "Path variable - " + getTypeDescription(javaType, paramName);
                    } else {
                        description = "Query parameter - " + getTypeDescription(javaType, paramName);
                    }

                    endpointInfo.addParameter(new EndpointInfo.ParameterInfo(paramName, paramType, isRequired, description, javaType));
                }
            }
        }
    }

    /**
     * Splits parameter string by comma, handling generic types correctly
     */
    private String[] splitParameters(String parametersString) {
        List<String> params = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : parametersString.toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (c == ',' && depth == 0) {
                params.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }

        if (!current.isEmpty()) {
            params.add(current.toString());
        }

        return params.toArray(new String[0]);
    }

    /**
     * Gets a human-readable description for a parameter type
     */
    private String getTypeDescription(String javaType, String paramName) {
        return switch (javaType) {
            case "Long", "Integer", "int", "long" -> {
                if (paramName.toLowerCase().contains("id")) {
                    yield paramName.replace("Id", "").toLowerCase() + " identifier";
                }
                yield "Integer value";
            }
            case "Double", "Float", "double", "float" -> "Decimal value";
            case "String" -> {
                if (paramName.toLowerCase().contains("name")) {
                    yield "Entity name";
                } else if (paramName.toLowerCase().contains("email")) {
                    yield "Email address";
                } else if (paramName.toLowerCase().contains("password")) {
                    yield "Password";
                } else if (paramName.toLowerCase().contains("username")) {
                    yield "Username";
                } else {
                    yield "Text value";
                }
            }
            case "Boolean", "boolean" -> "Boolean value";
            case "Date", "LocalDate", "LocalDateTime" -> "Date value";
            default -> "Entity identifier";
        };
    }

    /**
     * Adds parameters for standard CRUD endpoints
     */
    private void addStandardEndpointParameters(EntityInfo entityInfo) {
        // Add parameters for standard CRUD endpoints that have path variables
        for (EndpointInfo endpoint : entityInfo.getBehaviorEndpoints()) {
            String path = endpoint.getPath();

            // Handle standard GET by ID endpoint
            if (endpoint.getMethod().equals("GET") && path.endsWith("/{id}") && endpoint.getParameters().isEmpty()) {
                endpoint.addParameter("id", "number", true);
                endpoint.getParameters().getFirst().setDescription("Path variable - Entity identifier");
            }
        }
    }
}
