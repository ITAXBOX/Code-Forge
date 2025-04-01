package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpringAIRepository {

    public static void generateCompleteRepository(
            String projectName,
            String entityName,
            String behaviorServiceCode) throws IOException {

        // Validate inputs
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (entityName == null || entityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity name cannot be null or empty");
        }
        if (behaviorServiceCode == null || behaviorServiceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Behavior service code cannot be null or empty");
        }

        // Create directory structure if it doesn't exist
        Path repoDir = Paths.get(
                "./" + projectName + "/src/main/java/com/example/" +
                projectName.toLowerCase() + "/repositories/"
        );

        Path repoPath = repoDir.resolve(entityName + "Repository.java");

        // Extract repository variable name from service code
        String repoVar = detectRepositoryVariableName(behaviorServiceCode, entityName);

        // Extract method signatures
        Set<MethodSignature> usedMethods = new HashSet<>();
        usedMethods.addAll(extractServiceMethods(behaviorServiceCode));
        usedMethods.addAll(extractRepositoryCalls(behaviorServiceCode, repoVar, entityName));

        // Generate base repository content
        String repoContent = generateBaseRepositoryContent(projectName, entityName);

        // Add all extracted methods to the repository
        if (!usedMethods.isEmpty()) {
            String newMethods = usedMethods.stream()
                    .distinct()  // Remove duplicates
                    .map(MethodSignature::toString)
                    .collect(Collectors.joining("\n\n"));

            if (!newMethods.isEmpty()) {
                repoContent = repoContent.substring(0, repoContent.length() - 2) + // Remove closing brace
                              "\n\n" + newMethods + "\n}";
            }
        }

        // Write the repository file
        Files.writeString(repoPath, repoContent);
    }

    private static String detectRepositoryVariableName(String serviceCode, String entityName) {
        // Try common naming patterns
        final String s = entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Repository";
        String[] possiblePatterns = {
                s,
                entityName.toLowerCase() + "Repository",
                entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Repo",
                "repo"
        };

        for (String pattern : possiblePatterns) {
            if (serviceCode.contains(pattern + ".")) {
                return pattern;
            }
        }

        // Default pattern if none found
        return s;
    }

    private static String generateBaseRepositoryContent(String projectName, String entityName) {
        StringBuilder repositoryContent = new StringBuilder();

        // Add default methods for common entities
        if (entityName.equals("Role")) {
            repositoryContent.append("    Optional<Role> findByName(String name);\n");
        } else if (entityName.equals("User")) {
            repositoryContent.append("    boolean existsByUsername(String username);\n")
                    .append("    Optional<User> findByEmail(String email);\n")
                    .append("    Optional<User> findByUsername(String username);\n");
        }

        return "package com.example." + projectName.toLowerCase() + ".repositories;\n\n" +
               "import org.springframework.data.jpa.repository.JpaRepository;\n" +
               "import com.example." + projectName.toLowerCase() + ".models.*;\n" +
               "import org.springframework.stereotype.Repository;\n" +
               "import java.util.*;\n" +
               "import java.time.*;\n\n" +
               "@Repository\n" +
               "public interface " + entityName + "Repository extends JpaRepository<" +
               entityName + ", Long> {\n" +
               repositoryContent +
               "}\n";
    }

    private static Set<MethodSignature> extractServiceMethods(String serviceCode) {
        Set<MethodSignature> methods = new HashSet<>();
        Pattern pattern = Pattern.compile("@Transactional\\s+public\\s+(\\w+)\\s+(\\w+)\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(serviceCode);

        while (matcher.find()) {
            String returnType = matcher.group(1);
            String methodName = matcher.group(2);
            String params = matcher.group(3).trim();

            methods.add(new MethodSignature(returnType, methodName, parseParameters(params, serviceCode)));
        }
        return methods;
    }

    private static Set<MethodSignature> extractRepositoryCalls(String serviceCode, String repoVar, String entityName) {
        Set<MethodSignature> methods = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(repoVar) + "\\.(\\w+)\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(serviceCode);

        while (matcher.find()) {
            String methodName = matcher.group(1);
            String params = matcher.group(2).trim();
            String returnType = determineReturnType(methodName, entityName);
            methods.add(new MethodSignature(returnType, methodName, parseParameters(params, serviceCode)));
        }
        return methods;
    }

    private static String determineReturnType(String methodName, String entityName) {
        if (methodName.startsWith("find")) {
            if (methodName.matches("findAll[A-Z].*") ||
                methodName.matches("findBy[A-Z].*And[A-Z].*") ||
                methodName.matches("find[A-Z].*By[A-Z].*")) {
                return "List<" + entityName + ">";
            } else {
                return "Optional<" + entityName + ">";
            }
        }
        if (methodName.startsWith("count")) return "long";
        if (methodName.startsWith("exists")) return "boolean";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "void";
        if (methodName.equals("save")) return entityName;
        return "Object";
    }

    private static List<Param> parseParameters(String params, String contextCode) {
        if (params == null || params.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(params.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(p -> {
                    // Handle cases where parameter might just be a name without type
                    String[] parts = p.split("\\s+");

                    // Case 1: Just parameter name (e.g., "username")
                    if (parts.length == 1) {
                        String inferredType = inferParameterType(parts[0], contextCode);
                        return new Param(inferredType != null ? inferredType : "String", parts[0]);
                    }
                    // Case 2: Type and name (e.g., "String username")
                    else if (parts.length == 2) {
                        return new Param(parts[0], parts[1]);
                    }
                    // Case 3: Modifier + type + name (e.g., "final String username")
                    else if (parts.length >= 3) {
                        // Take the last two parts (type and name)
                        return new Param(parts[parts.length - 2], parts[parts.length - 1]);
                    }
                    // This should theoretically never happen due to previous filters
                    return new Param("Object", p);
                })
                .collect(Collectors.toList());
    }

    private static String inferParameterType(String paramName, String contextCode) {
        // First try to find explicit declaration in context
        Pattern declarationPattern = Pattern.compile(
                "(\\w+)\\s+" + Pattern.quote(paramName) + "\\s*[;=)]");
        Matcher matcher = declarationPattern.matcher(contextCode);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Then try common naming conventions
        if (paramName.endsWith("Id") || paramName.equals("id")) return "Long";
        if (paramName.endsWith("Name") || paramName.endsWith("name")) return "String";
        if (paramName.endsWith("Date")) return "LocalDate";
        if (paramName.endsWith("Time")) return "LocalTime";
        if (paramName.endsWith("DateTime")) return "LocalDateTime";
        if (paramName.endsWith("Flag") || paramName.endsWith("Active")) return "boolean";
        if (paramName.endsWith("Price") || paramName.endsWith("Amount")) return "BigDecimal";
        if (paramName.endsWith("Count") || paramName.endsWith("Quantity")) return "int";

        // Default to String for common cases
        if (paramName.matches("name|username|email|description|title|text|content")) {
            return "String";
        }

        // Final fallback
        return "Object";
    }

    private record MethodSignature(String returnType, String name, List<Param> parameters) {
        @Override
        public String toString() {
            String paramsString = parameters.stream()
                    .map(p -> p.type + " " + p.name)
                    .collect(Collectors.joining(", "));

            return "    " + returnType + " " + name + "(" + paramsString + ");";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodSignature that = (MethodSignature) o;
            return returnType.equals(that.returnType) &&
                   name.equals(that.name) &&
                   parameters.size() == that.parameters.size();
        }

        @Override
        public int hashCode() {
            return Objects.hash(returnType, name, parameters.size());
        }
    }

    private record Param(String type, String name) {
    }
}