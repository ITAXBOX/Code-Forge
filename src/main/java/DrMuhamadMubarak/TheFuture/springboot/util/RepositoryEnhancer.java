package DrMuhamadMubarak.TheFuture.springboot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RepositoryEnhancer {

    // 1. Define default JPA methods we should NEVER add
    private static final Set<String> DEFAULT_METHODS = Set.of(
            "save", "delete", "findById", "findAll",
            "count", "existsById", "flush", "deleteAll"
    );

    // 2. Main enhancement method
    public static void enhanceRepository(String projectName,
                                         String entityName,
                                         String serviceCode) throws IOException {

        // 3. Get repository file path
        Path repoPath = Paths.get(
                "./" + projectName + "/src/main/java/com/example/" +
                projectName.toLowerCase() + "/repositories/" +
                entityName + "Repository.java"
        );

        // 4. Skip if repository doesn't exist
        if (!Files.exists(repoPath)) return;

        // 5. Find all USED repository methods in service
        Set<String> usedMethods = findUsedMethods(serviceCode, entityName);
        if (usedMethods.isEmpty()) return;

        // 6. Read existing repository code
        String repoCode = Files.readString(repoPath);

        // 7. Check which methods are MISSING
        Set<String> missingMethods = new HashSet<>();
        for (String method : usedMethods) {
            if (!repoCode.contains(method + "(")) { // Check if method exists
                missingMethods.add(method);
            }
        }

        // 8. Add missing methods
        if (!missingMethods.isEmpty()) {
            String methodsToAdd = missingMethods.stream()
                    .map(m -> "    " + m + ";\n")
                    .collect(Collectors.joining());

            String updatedCode = repoCode.replace("}", methodsToAdd + "}");
            Files.write(repoPath, updatedCode.getBytes());
        }
    }

    // 9. Find all repository method calls in service code
    private static Set<String> findUsedMethods(String serviceCode, String entityName) {
        Set<String> methods = new HashSet<>();
        Pattern pattern = Pattern.compile(
                entityName + "Repository\\.(\\w+?)\\("
        );

        Matcher matcher = pattern.matcher(serviceCode);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!DEFAULT_METHODS.contains(methodName)) {
                methods.add(methodName + "()"); // Simplified signature
            }
        }
        return methods;
    }
}