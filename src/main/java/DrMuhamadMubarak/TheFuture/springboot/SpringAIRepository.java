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

        Set<MethodSignature> serviceMethodCalls = extractRepositoryCalls(behaviorServiceCode, repoVar, entityName);
        Set<MethodSignature> serviceDeclaredMethods = extractServiceMethods(behaviorServiceCode);

        Set<MethodSignature> allMethods = new HashSet<>();
        for (MethodSignature m : serviceMethodCalls) {
            if (!DEFAULT_JPA_METHODS.contains(m.name) && !serviceDeclaredMethods.contains(m)) {
                allMethods.add(m);
            }
        }

        String repoContent = generateBaseRepositoryContent(projectName, entityName);

        if (!allMethods.isEmpty()) {
            String customMethods = allMethods.stream()
                    .map(MethodSignature::toString)
                    .distinct()
                    .collect(Collectors.joining("\n\n"));

            repoContent = repoContent.replace("}", "\n\n" + customMethods + "\n}");
        }

        Files.writeString(repoPath, repoContent);
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

    private static Set<MethodSignature> extractServiceMethods(String code) {
        Set<MethodSignature> methods = new HashSet<>();
        Pattern pattern = Pattern.compile("public\\s+(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String returnType = matcher.group(1);
            String name = matcher.group(2);
            List<Param> params = parseParameters(matcher.group(3), code);
            methods.add(new MethodSignature(returnType, name, params));
        }
        return methods;
    }

    private static Set<MethodSignature> extractRepositoryCalls(String code, String repoVar, String entityName) {
        Set<MethodSignature> methods = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(repoVar) + "\\.(\\w+)\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(1);
            String rawParams = matcher.group(2);
            String returnType = determineReturnType(name, entityName);
            List<Param> params = parseParameters(rawParams, code);
            methods.add(new MethodSignature(returnType, name, params));
        }
        return methods;
    }

    private static String determineReturnType(String methodName, String entityName) {
        if (methodName.startsWith("find")) {
            if (methodName.matches("findAll[A-Z].*")) return "List<" + entityName + ">";
            return "Optional<" + entityName + ">";
        }
        if (methodName.startsWith("count")) return "long";
        if (methodName.startsWith("exists")) return "boolean";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "void";
        if (methodName.equals("save")) return entityName;
        return "Object";
    }

    private static List<Param> parseParameters(String rawParams, String contextCode) {
        if (rawParams == null || rawParams.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(rawParams.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(p -> {
                    String[] parts = p.split("\\s+");
                    if (parts.length == 1) {
                        String inferred = inferParameterType(parts[0], contextCode);
                        return new Param(inferred != null ? inferred : "String", parts[0]);
                    } else if (parts.length == 2) {
                        return new Param(parts[0], parts[1]);
                    } else {
                        return new Param(parts[parts.length - 2], parts[parts.length - 1]);
                    }
                }).collect(Collectors.toList());
    }

    private static String inferParameterType(String name, String code) {
        Pattern p = Pattern.compile("(\\w+)\\s+" + Pattern.quote(name) + "\\s*[\";=)]");
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
            String paramStr = params.stream().map(p -> p.type + " " + p.name).collect(Collectors.joining(", "));
            return "    " + returnType + " " + name + "(" + paramStr + ");";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodSignature(String type, String name1, List<Param> params1))) return false;
            return returnType.equals(type) && name.equals(name1) && params.equals(params1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(returnType, name, params);
        }
    }

    private record Param(String type, String name) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Param(String type1, String name1))) return false;
            return type.equals(type1) && name.equals(name1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }
    }
}
