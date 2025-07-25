package DrMuhamadMubarak.TheFuture.backend.springboot;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class containing helper methods and constants for Spring repository generation.
 * This class provides utility functions for type inference, method analysis, and parameter parsing.
 */
public class SpringAIRepositoryUtils {

    // Default JPA methods that should not be duplicated
    public static final Set<String> DEFAULT_JPA_METHODS = Set.of(
            "findAll", "findById", "save", "deleteById", "delete", "existsById", "count"
    );

    // Known method name prefixes and their return types
    public static final Map<String, String> METHOD_PREFIX_RETURN_TYPES = Map.of(
            "findAll", "List",
            "findBy", "Optional",
            "getBy", "Optional",
            "findFirstBy", "Optional",
            "countBy", "Long",
            "existsBy", "boolean",
            "deleteBy", "void"
    );

    // Methods that should be excluded from duplicate checking
    public static final Set<String> STANDARD_METHODS = Set.of(
            "existsByUsername",
            "findByEmail",
            "findByUsername",
            "findByName"
    );

    // Collection indicator method names - methods that should always return collections
    public static final Set<String> COLLECTION_METHOD_PATTERNS = Set.of(
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
    public static final Set<String> SINGLE_ENTITY_METHOD_PATTERNS = Set.of(
            "findByUsername",
            "findByEmail",
            "findByPhone",
            "findByIdentifier",
            "findByCode",
            "findByName",
            "findById",
            "findOneBy(.*)"
    );

    /**
     * Detects the repository variable name used in service code.
     */
    public static String detectRepositoryVariableName(String serviceCode, String entityName) {
        String base = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
        String[] guesses = {base + "Repository", base + "Repo", "repo"};

        for (String guess : guesses) {
            if (serviceCode.contains(guess + ".")) return guess;
        }
        return base + "Repository";
    }

    /**
     * Extracts existing method signatures from repository content to avoid duplicates.
     */
    public static Set<String> extractExistingMethodSignatures(String repoContent) {
        Set<String> signatures = new HashSet<>();
        Pattern methodPattern = Pattern.compile("\\s+(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\(([^)]*?)\\);");
        Matcher methodMatcher = methodPattern.matcher(repoContent);

        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(2);
            signatures.add(methodName);
        }

        return signatures;
    }

    /**
     * Checks if a method is a duplicate based on existing methods.
     */
    public static boolean isDuplicateMethod(MethodSignature method, Set<String> existingMethods) {
        // If it's a standard method that's already defined, consider it a duplicate
        if (STANDARD_METHODS.contains(method.name()) && existingMethods.contains(method.name())) {
            return true;
        }
        return existingMethods.contains(method.name());
    }

    /**
     * Analyzes repository method usage in service code to determine return types.
     */
    public static Map<String, String> analyzeRepositoryMethodUsage(String code, String repoVar, String entityName) {
        Map<String, String> returnTypes = new HashMap<>();

        // Pattern to capture repository method usage in a broader context
        Pattern pattern = Pattern.compile(
                "(?:(?:Optional|List|Collection|Set)<[^>]*>|[\\w<>,\\s]+)\\s+\\w+\\s*=\\s*" +
                        Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)|" +
                        Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)\\.(?:isPresent|get|orElse|stream|collect|size|isEmpty|forEach)|" +
                        "return\\s+" + Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)|" +
                        "for\\s*\\([^)]*\\s*:\\s*" + Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)\\)"
        );

        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String repoMethodName = matcher.group(1) != null ? matcher.group(1) :
                    (matcher.group(2) != null ? matcher.group(2) :
                            (matcher.group(3) != null ? matcher.group(3) : matcher.group(4)));

            // The context of the usage helps determine the expected return type
            String methodContext = matcher.group(0);
            String contextualReturnType = inferReturnTypeFromContext(methodContext, repoMethodName, entityName);

            if (contextualReturnType != null) {
                returnTypes.put(repoMethodName, contextualReturnType);
            }
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

    /**
     * Infers the return type of repository method based on how it's used in the service code.
     * This is critical for ensuring the repository interface matches the expectation of the service.
     */
    public static String inferReturnTypeFromContext(String methodContext, String methodName, String entityName) {
        // Check for Optional usage
        boolean usesOptional = methodContext.contains("Optional<") ||
                methodContext.contains(".isPresent()") ||
                methodContext.contains(".get()") ||
                methodContext.contains(".orElse") ||
                methodContext.contains(".orElseGet") ||
                methodContext.contains(".orElseThrow");

        // Check for List/Collection usage
        boolean usesList = methodContext.contains("List<") ||
                methodContext.contains("Collection<") ||
                methodContext.contains(".stream()") ||
                methodContext.contains(".forEach") ||
                methodContext.contains(".collect(") ||
                methodContext.contains(".size()") ||
                methodContext.contains(".isEmpty()") ||
                methodContext.contains("for (") ||
                methodContext.contains("for(");

        // Check for direct return which indicates matching return types
        boolean isDirectReturn = methodContext.startsWith("return");

        // Special case for boolean methods
        if (methodName.startsWith("existsBy") || methodName.startsWith("exists") ||
                methodContext.contains("boolean")) {
            return "boolean";
        }

        // Special case for count methods
        if (methodName.startsWith("countBy") || methodName.startsWith("count") ||
                methodContext.contains("Long") || methodContext.contains("Integer") ||
                methodContext.contains("long") || methodContext.contains("int")) {
            return "Long";
        }

        // Determine return type based on the usage context
        if (usesOptional && !usesList) {
            return "Optional<" + entityName + ">";
        } else if (usesList) {
            return "List<" + entityName + ">";
        } else if (isDirectReturn) {
            // If directly returned, check method name patterns first
            String inferredType = inferReturnTypeFromMethodName(methodName, entityName);

            return Objects.requireNonNullElseGet(inferredType, () -> "Optional<" + entityName + ">");
        }
        // If we can't determine from context, use method name patterns
        return inferReturnTypeFromMethodName(methodName, entityName);
    }

    /**
     * Infers return type based on method name patterns.
     */
    public static String inferReturnTypeFromMethodName(String methodName, String entityName) {
        // Check if method matches any of the collection method patterns
        for (String pattern : COLLECTION_METHOD_PATTERNS) {
            if (methodName.matches(pattern.replace("(.*)", ".*"))) {
                return "List<" + entityName + ">";
            }
        }

        // Check if method matches any of the single entity method patterns
        for (String pattern : SINGLE_ENTITY_METHOD_PATTERNS) {
            if (methodName.matches(pattern.replace("(.*)", ".*")) || methodName.equals(pattern)) {
                return "Optional<" + entityName + ">";
            }
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

        // Methods that return an entity from a relationship often return a List
        // But only if they haven't been caught by the single entity patterns above
        if (methodName.startsWith("findBy") && !methodName.startsWith("findById")) {
            // These methods typically return lists when the parameter is not a unique identifier
            return "List<" + entityName + ">";
        }

        return null;
    }

    /**
     * Extracts repository methods from service code.
     */
    public static Set<MethodSignature> extractRepositoryMethods(
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

    /**
     * Generates base repository content with common methods.
     */
    public static String generateBaseRepositoryContent(String projectName, String entityName) {
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

    /**
     * Parses method parameters from raw parameter string.
     */
    public static List<Param> parseParameters(String rawParams, String contextCode) {
        if (rawParams == null || rawParams.trim().isEmpty()) return Collections.emptyList();

        List<String> paramStrings = getStrings(rawParams);

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

    private static List<String> getStrings(String rawParams) {
        List<String> paramStrings = new ArrayList<>();
        int angleBracketDepth = 0;
        StringBuilder currentParam = new StringBuilder();

        return getStrings(rawParams, paramStrings, angleBracketDepth, currentParam);
    }

    static List<String> getStrings(String rawParams, List<String> paramStrings, int angleBracketDepth, StringBuilder currentParam) {
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
        return paramStrings;
    }

    /**
     * Infers parameter type based on parameter name and context.
     */
    public static String inferParameterType(String name, String code) {
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

    /**
     * Record representing a method signature with return type, name, and parameters.
     */
    public record MethodSignature(String returnType, String name, List<Param> params) {
        @Override
        public String toString() {
            String paramStr = params.stream()
                    .map(p -> p.type + " " + p.name)
                    .collect(Collectors.joining(", "));
            return "    " + returnType + " " + name + "(" + paramStr + ");";
        }
    }

    /**
     * Record representing a method parameter with type and name.
     */
    public record Param(String type, String name) {
    }
}
