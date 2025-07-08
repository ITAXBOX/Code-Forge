package DrMuhamadMubarak.TheFuture.backend.springboot;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static DrMuhamadMubarak.TheFuture.backend.springboot.SpringAIRepositoryUtils.*;

public class SpringAIRepository {

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
}
