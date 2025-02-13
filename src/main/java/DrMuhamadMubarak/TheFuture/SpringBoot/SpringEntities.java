package DrMuhamadMubarak.TheFuture.SpringBoot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DrMuhamadMubarak.TheFuture.utils.Utils.capitalize;

public class SpringEntities {
    public static void generateSpringEntityClasses(String projectName, String[] entities) throws IOException {
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
}
