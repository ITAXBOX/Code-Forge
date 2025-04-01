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

    private static final Set<String> DEFAULT_JPA_METHODS = Set.of(
            "save", "saveAll", "delete", "deleteById", "deleteAll",
            "findById", "findAll", "count", "existsById", "flush"
    );

    public static void generateCompleteRepository(
            String projectName,
            String entityName,
            String behaviorServiceCode) throws IOException {

        // Create directory structure if it doesn't exist
        Path repoDir = Paths.get(
                "./" + projectName + "/src/main/java/com/example/" +
                projectName.toLowerCase() + "/repositories/"
        );

        Path repoPath = repoDir.resolve(entityName + "Repository.java");

        // 1. Extract only used methods from behavior service
        Set<MethodSignature> usedMethods = extractUsedMethods(behaviorServiceCode, entityName);

        // 2. Generate base repository content
        String repoContent = generateBaseRepositoryContent(projectName, entityName);

        // 3. Add used methods that aren't default JPA methods
        if (!usedMethods.isEmpty()) {
            String newMethods = usedMethods.stream()
                    .filter(method -> !DEFAULT_JPA_METHODS.contains(method.name()))
                    .map(MethodSignature::toString)
                    .collect(Collectors.joining("\n\n"));

            if (!newMethods.isEmpty()) {
                repoContent = repoContent.substring(0, repoContent.length() - 2) + // Remove closing brace
                              "\n\n" + newMethods + "\n}";
            }
        }

        // 4. Write the repository file
        Files.writeString(repoPath, repoContent);
    }

    private static String generateBaseRepositoryContent(String projectName, String entityName) {
        return "package com.example." + projectName.toLowerCase() + ".repositories;\n\n" +
               "import org.springframework.data.jpa.repository.JpaRepository;\n" +
               "import com.example." + projectName.toLowerCase() + ".models.*;\n" +
               "import org.springframework.stereotype.Repository;\n" +
               "import java.util.*;\n" +
               "import java.time.*;\n\n" +
               "@Repository\n" +
               "public interface " + entityName + "Repository extends JpaRepository<" +
               entityName + ", Long> {\n" +
               "}\n";  // Initial empty interface
    }

    private static Set<MethodSignature> extractUsedMethods(String serviceCode, String entityName) {
        Set<MethodSignature> methods = new HashSet<>();
        Pattern pattern = Pattern.compile(
                "\\b" + entityName + "Repository\\.(\\w+?)\\(([^)]*)"
        );

        Matcher matcher = pattern.matcher(serviceCode);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            String params = matcher.group(2).trim();

            if (!DEFAULT_JPA_METHODS.contains(methodName)) {
                methods.add(buildMethodSignature(entityName, methodName, params));
            }
        }
        return methods;
    }

    private static MethodSignature buildMethodSignature(String entityName, String methodName, String params) {
        return new MethodSignature(
                determineReturnType(methodName, entityName),
                methodName,
                parseParameters(params)
        );
    }

    private static String determineReturnType(String methodName, String entityName) {
        if (methodName.startsWith("find")) {
            return methodName.matches("findAll[A-Z].*|findBy[A-Z].*And[A-Z].*")
                    ? "List<" + entityName + ">"
                    : "Optional<" + entityName + ">";
        }
        if (methodName.startsWith("count")) return "long";
        if (methodName.startsWith("exists")) return "boolean";
        return "void";
    }

    private static List<Param> parseParameters(String params) {
        if (params.isEmpty()) return Collections.emptyList();

        return Arrays.stream(params.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(p -> {
                    if (p.matches("[A-Z]\\w*")) {
                        return new Param(p, p.substring(0, 1).toLowerCase() + p.substring(1));
                    }
                    if (p.endsWith("Id") || p.matches("\\d+")) {
                        return new Param("Long", p);
                    }
                    if (p.matches("\".*\"")) {
                        return new Param("String", p.replace("\"", ""));
                    }
                    return new Param("String", p);
                })
                .collect(Collectors.toList());
    }

    private record MethodSignature(String returnType, String name, List<Param> parameters) {
        @Override
        public String toString() {
            return "    " + returnType + " " + name + "(" +
                   parameters.stream()
                           .map(p -> p.type + " " + p.name)
                           .collect(Collectors.joining(", ")) +
                   ");";
        }
    }

    private record Param(String type, String name) {
    }
}