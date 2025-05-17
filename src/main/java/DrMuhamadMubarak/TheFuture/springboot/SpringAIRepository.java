package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class SpringAIRepository {

    // Default JPA methods that should not be duplicated
    private static final Set<String> DEFAULT_JPA_METHODS = Set.of(
            "findAll", "findById", "save", "deleteById", "delete", "existsById", "count"
    );

    // Known method name prefixes and their return types
    private static final Map<String, String> METHOD_PREFIX_RETURN_TYPES = Map.of(
            "findAll", "List",
            "findBy", "Optional",
            "getBy", "Optional",
            "findFirstBy", "Optional",
            "countBy", "Long",
            "existsBy", "boolean",
            "deleteBy", "void"
    );

    // Methods that should be excluded from duplicate checking
    private static final Set<String> STANDARD_METHODS = Set.of(
            "existsByUsername",
            "findByEmail",
            "findByUsername",
            "findByName"
    );

    // Collection indicator method names - methods that should always return collections
    private static final Set<String> COLLECTION_METHOD_PATTERNS = Set.of(
            "findAll",
            "findBy(.*)Between",
            "findBy(.*)In",
            "findBy(.*)After",
            "findBy(.*)Before",
            "findBy(.*)GreaterThan",
            "findBy(.*)LessThan",
            "findBy(.*)Containing",
            "findBy(.*)NotIn",
            "findBy(.*)Like",
            "findBy(.*)OrderBy"
    );

    // Methods that should specifically return Optional<Entity> rather than List
    private static final Set<String> SINGLE_ENTITY_METHOD_PATTERNS = Set.of(
            "findByUsername",
            "findByEmail",
            "findByPhone",
            "findByIdentifier",
            "findByCode",
            "findOneBy(.*)"
    );

    public static void generateCompleteRepository(
            String projectName,
            String entityName,
            String behaviorServiceCode) throws IOException {

        if (projectName == null || projectName.trim().isEmpty() ||
                entityName == null || entityName.trim().isEmpty() ||
                behaviorServiceCode == null || behaviorServiceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Inputs must not be null or empty");
        }

        Path repoDir = Paths.get("./" + projectName + "/src/main/java/com/example/" +
                projectName.toLowerCase() + "/repositories/");
        Files.createDirectories(repoDir);

        Path repoPath = repoDir.resolve(entityName + "Repository.java");
        String repoVar = detectRepositoryVariableName(behaviorServiceCode, entityName);

        Map<String, String> repositoryMethodReturnTypes = analyzeRepositoryMethodUsage(behaviorServiceCode, repoVar, entityName);
        Set<MethodSignature> repositoryMethods = extractRepositoryMethods(behaviorServiceCode, repoVar, entityName, repositoryMethodReturnTypes);

        String repoContent = generateBaseRepositoryContent(projectName, entityName);

        if (!repositoryMethods.isEmpty()) {
            // Keep track of methods we've already added to avoid duplicates
            Set<String> addedMethodSignatures = extractExistingMethodSignatures(repoContent);

            String customMethods = repositoryMethods.stream()
                    .filter(method -> !isDuplicateMethod(method, addedMethodSignatures))
                    .map(MethodSignature::toString)
                    .collect(Collectors.joining("\n\n"));

            if (!customMethods.isEmpty()) {
                repoContent = repoContent.replace("}", "\n\n" + customMethods + "\n}");
            }
        }

        Files.writeString(repoPath, repoContent);
    }

    private static Set<String> extractExistingMethodSignatures(String repoContent) {
        Set<String> signatures = new HashSet<>();
        Pattern methodPattern = Pattern.compile("\\s+(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\(([^)]*?)\\);");
        Matcher methodMatcher = methodPattern.matcher(repoContent);

        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(2);
            signatures.add(methodName);
        }

        return signatures;
    }

    private static boolean isDuplicateMethod(MethodSignature method, Set<String> existingMethods) {
        // If it's a standard method that's already defined, consider it a duplicate
        if (STANDARD_METHODS.contains(method.name()) && existingMethods.contains(method.name())) {
            return true;
        }
        return existingMethods.contains(method.name());
    }

    private static Map<String, String> analyzeRepositoryMethodUsage(String code, String repoVar, String entityName) {
        Map<String, String> returnTypes = new HashMap<>();

        // Pattern to capture service method return type and repository method usage
        Pattern pattern = Pattern.compile(
                "@Transactional\\s+public\\s+([\\w<>\\s,]+)\\s+\\w+\\([^)]*\\)\\s*\\{([^}]*?)" +
                        Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)([^}]*)}");

        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String serviceReturnType = matcher.group(1).trim();
            String beforeMethodCall = matcher.group(2);
            String repoMethodName = matcher.group(3);
            String afterMethodCall = matcher.group(4);

            String returnType = determineReturnType(serviceReturnType, beforeMethodCall + afterMethodCall,
                    repoMethodName, entityName);
            returnTypes.put(repoMethodName, returnType);
        }

        // Look for repository methods outside @Transactional methods too
        Pattern repoMethodPattern = Pattern.compile(
                "\\b" + Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)");
        Matcher repoMethodMatcher = repoMethodPattern.matcher(code);

        while (repoMethodMatcher.find()) {
            String repoMethodName = repoMethodMatcher.group(1);
            if (!returnTypes.containsKey(repoMethodName)) {
                returnTypes.put(repoMethodName, inferReturnTypeFromMethodName(repoMethodName, entityName));
            }
        }

        return returnTypes;
    }

    private static String determineReturnType(String serviceReturnType, String methodBody,
                                              String repoMethodName, String entityName) {
        // Check if the method name tells us the return type
        String inferredType = inferReturnTypeFromMethodName(repoMethodName, entityName);
        if (inferredType != null) {
            return inferredType;
        }

        // Check if service returns a list or collection type
        boolean serviceReturnsList = serviceReturnType.startsWith("List<") ||
                serviceReturnType.startsWith("Collection<") ||
                serviceReturnType.contains("Iterable<");

        // Check if method body uses collection operations
        boolean usesCollectionOps = methodBody.contains(".addAll(") ||
                methodBody.contains("new ArrayList<") ||
                methodBody.contains("Arrays.asList(") ||
                methodBody.contains(".add(") ||
                methodBody.contains("stream().") ||
                methodBody.contains(".forEach") ||
                methodBody.contains(".collect(");

        if (serviceReturnsList || usesCollectionOps) {
            return "List<" + entityName + ">";
        }

        // Check for boolean methods
        if (serviceReturnType.equals("boolean") || methodBody.contains("return true") ||
                methodBody.contains("return false")) {
            return "boolean";
        }

        // Check for count methods
        if (serviceReturnType.equals("long") || serviceReturnType.equals("int") ||
                serviceReturnType.equals("Integer") || serviceReturnType.equals("Long")) {
            return "Long";
        }

        // Default to Optional for singular entity returns
        return "Optional<" + entityName + ">";
    }

    private static String inferReturnTypeFromMethodName(String methodName, String entityName) {
        // Check if method matches any of the collection method patterns
        for (String pattern : COLLECTION_METHOD_PATTERNS) {
            if (methodName.matches(pattern.replace("(.*)", ".*"))) {
                return "List<" + entityName + ">";
            }
        }

        // Check if method matches any of the single entity method patterns
        for (String pattern : SINGLE_ENTITY_METHOD_PATTERNS) {
            if (methodName.matches(pattern.replace("(.*)", ".*"))) {
                return "Optional<" + entityName + ">";
            }
        }

        // Methods that return an entity from a relationship often return a List
        if (methodName.startsWith("findBy") && !methodName.startsWith("findById") &&
            !methodName.contains("Username") && !methodName.contains("Email")) {
            // These methods typically return lists when the parameter is singular
            return "List<" + entityName + ">";
        }

        // Direct method prefix matching
        for (Map.Entry<String, String> entry : METHOD_PREFIX_RETURN_TYPES.entrySet()) {
            if (methodName.startsWith(entry.getKey())) {
                String returnType = entry.getValue();
                if (returnType.equals("List")) {
                    return "List<" + entityName + ">";
                } else if (returnType.equals("Optional")) {
                    return "Optional<" + entityName + ">";
                } else {
                    return returnType;
                }
            }
        }

        // Generic inference
        if (methodName.startsWith("findAll")) {
            return "List<" + entityName + ">";
        }

        return null;
    }

    private static Set<MethodSignature> extractRepositoryMethods(
            String code, String repoVar, String entityName,
            Map<String, String> returnTypes) {

        Set<MethodSignature> methods = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(repoVar) + "\\.(\\w+)\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(1);
            if (DEFAULT_JPA_METHODS.contains(name)) continue;

            String rawParams = matcher.group(2);
            List<Param> params = parseParameters(rawParams, code);

            String returnType = returnTypes.getOrDefault(name, inferReturnTypeFromMethodName(name, entityName));
            if (returnType == null) {
                returnType = "Optional<" + entityName + ">";
            }

            methods.add(new MethodSignature(returnType, name, params));
        }
        return methods;
    }

    private static String detectRepositoryVariableName(String serviceCode, String entityName) {
        String base = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
        String[] guesses = {base + "Repository", base + "Repo", "repo"};

        for (String guess : guesses) {
            if (serviceCode.contains(guess + ".")) return guess;
        }
        return base + "Repository";
    }

    private static String generateBaseRepositoryContent(String projectName, String entityName) {
        StringBuilder base = new StringBuilder();
        if (entityName.equals("Role")) {
            base.append("    Optional<Role> findByName(String name);\n");
        } else if (entityName.equals("User")) {
            base.append("    boolean existsByUsername(String username);\n")
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
                "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                base + "}\n";
    }

    private static List<Param> parseParameters(String rawParams, String contextCode) {
        if (rawParams == null || rawParams.trim().isEmpty()) return Collections.emptyList();

        List<String> paramStrings = new ArrayList<>();
        int angleBracketDepth = 0;
        StringBuilder currentParam = new StringBuilder();

        for (char c : rawParams.toCharArray()) {
            if (c == '<') angleBracketDepth++;
            if (c == '>') angleBracketDepth--;

            if (c == ',' && angleBracketDepth == 0) {
                paramStrings.add(currentParam.toString().trim());
                currentParam = new StringBuilder();
            } else {
                currentParam.append(c);
            }
        }
        if (!currentParam.isEmpty()) {
            paramStrings.add(currentParam.toString().trim());
        }

        return paramStrings.stream()
                .map(p -> {
                    String[] parts = p.split("\\s+");
                    if (parts.length == 1) {
                        String inferred = inferParameterType(parts[0], contextCode);
                        return new Param(inferred != null ? inferred : "String", parts[0]);
                    } else if (parts.length == 2) {
                        return new Param(parts[0], parts[1]);
                    } else {
                        String type = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
                        return new Param(type, parts[parts.length - 1]);
                    }
                })
                .collect(Collectors.toList());
    }

    private static String inferParameterType(String name, String code) {
        Pattern p = Pattern.compile("(\\w+(?:<[^>]+>)?)\\s+" + Pattern.quote(name) + "\\s*[;=),]");
        Matcher m = p.matcher(code);
        if (m.find()) return m.group(1);

        if (name.endsWith("Id")) return "Long";
        if (name.endsWith("Name")) return "String";
        if (name.endsWith("Date")) return "LocalDate";
        if (name.endsWith("Time")) return "LocalTime";
        if (name.endsWith("Flag") || name.endsWith("Active")) return "boolean";
        return "String";
    }

    private record MethodSignature(String returnType, String name, List<Param> params) {
        @Override
        public String toString() {
            String paramStr = params.stream()
                    .map(p -> p.type + " " + p.name)
                    .collect(Collectors.joining(", "));
            return "    " + returnType + " " + name + "(" + paramStr + ");";
        }
    }

    private record Param(String type, String name) {
    }
}