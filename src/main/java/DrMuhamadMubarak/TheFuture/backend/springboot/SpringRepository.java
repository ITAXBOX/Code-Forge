package DrMuhamadMubarak.TheFuture.backend.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpringRepository {
    public static void generateSpringRepositoryClass(String projectName, String entityName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/repositories";
        Path repositoryFilePath = Paths.get(baseDir, entityName + "Repository.java");

        // Base repository content
        StringBuilder repositoryContent = new StringBuilder();
        repositoryContent.append("package com.example.").append(projectName.toLowerCase()).append(".repositories;\n\n")
                .append("import org.springframework.data.jpa.repository.JpaRepository;\n")
                .append("import com.example.").append(projectName.toLowerCase()).append(".models.").append(entityName).append(";\n")
                .append("import org.springframework.stereotype.Repository;\n")
                .append("import java.util.Optional;\n\n")
                .append("@Repository\n")
                .append("public interface ").append(entityName).append("Repository extends JpaRepository<").append(entityName).append(", Long> {\n");

        // Add custom methods based on the entity name
        if (entityName.equals("Role")) {
            repositoryContent.append("    Optional<Role> findByName(String name);\n");
        } else if (entityName.equals("User")) {
            repositoryContent.append("    boolean existsByUsername(String username);\n")
                    .append("    Optional<User> findByEmail(String email);\n");
        }

        repositoryContent.append("}\n");

        // Write the repository file
        Files.writeString(repositoryFilePath, repositoryContent.toString());
    }
}