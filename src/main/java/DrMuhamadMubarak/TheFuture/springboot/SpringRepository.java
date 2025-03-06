package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpringRepository {
    public static void generateSpringRepositoryClass(String projectName, String entityName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/repositories";
        Path repositoryFilePath = Paths.get(baseDir, entityName + "Repository.java");

        // Define the repository content with @Repository annotation and custom query method
        String repositoryContent = "package com.example." + projectName.toLowerCase() + ".repositories;\n\n" +
                                   "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                                   "import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n" +
                                   "import org.springframework.stereotype.Repository;\n" +
                                   "import java.util.List;\n\n" +
                                   "@Repository\n" +
                                   "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                                   "}\n";

        // Write the repository file
        Files.writeString(repositoryFilePath, repositoryContent);
    }
}
