package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DrMuhamadMubarak.TheFuture.utils.StringUtils.capitalizeFirstLetter;

public class SpringEntities {
    public static void generateSpringEntityClass(String projectName, String entity) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models";

        String className = capitalizeFirstLetter(entity);
        String classContent;

        // Check if the entity is "Role" to add the constructor
        if (className.equals("Role")) {
            classContent = String.format("""
                    package com.example.%s.models;
                    
                    import lombok.*;
                    import jakarta.persistence.*;
                    import java.time.*;
                    import java.util.*;
                    
                    @Getter
                    @Setter
                    @AllArgsConstructor
                    @NoArgsConstructor
                    @Entity
                    public class %s {
                        public %s(String name) {
                            this.name = name;
                        }
                    }
                    """, projectName.toLowerCase(), className, className);
        } else {
            classContent = String.format("""
                    package com.example.%s.models;
                    
                    import lombok.*;
                    import jakarta.persistence.*;
                    import java.time.*;
                    import java.util.*;
                    
                    @Getter
                    @Setter
                    @AllArgsConstructor
                    @NoArgsConstructor
                    @Entity
                    public class %s {
                    
                    }
                    """, projectName.toLowerCase(), className);
        }

        // Write the class content to the file
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }
}