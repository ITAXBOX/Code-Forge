package DrMuhamadMubarak.TheFuture.Service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@Service
public class EntitiesService {
    private String[] entities;

    public void generateEntityClasses(String projectName, String[] entities) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models";

        for (String entity : entities) {
            String className = capitalize(entity);
            String classContent = String.format("""
                    package com.example.%s.models;
                    
                    import lombok.*;
                    import jakarta.persistence.*;
                    import java.time.*;
                    
                    @Getter
                    @Setter
                    @AllArgsConstructor
                    @NoArgsConstructor
                    @Entity
                    public class %s {
                    
                    }
                    """, projectName.toLowerCase(), className);
            Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public void generateRepositoryClass(String projectName, String entityName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/repositories";
        Path repositoryFilePath = Paths.get(baseDir, entityName + "Repository.java");

        // Create the directory if it doesn't exist
        Files.createDirectories(Paths.get(baseDir));

        // Define the repository content with @Repository annotation
        String repositoryContent = "package com.example." + projectName.toLowerCase() + ".repositories;\n\n" +
                                   "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                                   "import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n" +
                                   "import org.springframework.stereotype.Repository;\n\n" +
                                   "@Repository\n" +
                                   "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                                   "}\n";

        // Write the repository file
        Files.writeString(repositoryFilePath, repositoryContent);
    }


}
