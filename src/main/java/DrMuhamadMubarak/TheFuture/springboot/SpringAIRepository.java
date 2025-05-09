package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class SpringAIRepository {

    private static final Set<String> DEFAULT_JPA_METHODS = Set.of(
            "findAll", "findById", "save", "deleteById", "delete", "existsById", "count"
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
            String customMethods = repositoryMethods.stream()
                    .map(MethodSignature::toString)
                    .collect(Collectors.joining("\n\n"));

            repoContent = repoContent.replace("}", "\n\n" + customMethods + "\n}");
        }

        Files.writeString(repoPath, repoContent);
    }

    private static Map<String, String> analyzeRepositoryMethodUsage(String code, String repoVar, String entityName) {
        Map<String, String> returnTypes = new HashMap<>();
        // Pattern to capture service method return type and repository method usage
        Pattern pattern = Pattern.compile(
                "@Transactional\\s+public\\s+([\\w<>]+)\\s+\\w+\\([^)]*\\)\\s*\\{[^}]*?" +
                Pattern.quote(repoVar) + "\\.(\\w+)\\([^)]*\\)([^}]*)}");

        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String serviceReturnType = matcher.group(1);
            String repoMethodName = matcher.group(2);
            String methodBody = matcher.group(3);

            boolean returnsList = serviceReturnType.startsWith("List<") ||
                                  methodBody.contains(".addAll(") ||
                                  methodBody.contains("new ArrayList<") ||
                                  methodBody.contains("Arrays.asList(") ||
                                  methodBody.contains(".add(");

            returnTypes.put(repoMethodName, returnsList ? "List<" + entityName + ">" : "Optional<" + entityName + ">");
        }
        return returnTypes;
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

            String returnType = returnTypes.getOrDefault(name, "Optional<" + entityName + ">");
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

    private record Param(String type, String name) {}
}