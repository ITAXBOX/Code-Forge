package DrMuhamadMubarak.TheFuture.Service;

import DrMuhamadMubarak.TheFuture.DTO.AttributeDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        // Define the repository content with @Repository annotation and custom query method
        String repositoryContent = "package com.example." + projectName.toLowerCase() + ".repositories;\n\n" +
                                   "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                                   "import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n" +
                                   "import org.springframework.stereotype.Repository;\n" +
                                   "import java.util.List;\n\n" +
                                   "@Repository\n" +
                                   "public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {\n" +
                                   "    List<" + entityName + "> findByDisplayInListTrue();\n" +
                                   "}\n";

        // Write the repository file
        Files.writeString(repositoryFilePath, repositoryContent);
    }

    public void generateServiceClass(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String className = entityName + "Service";
        String fileName = projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/services/" + className + ".java";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("package com.example." + projectName.toLowerCase() + ".services;\n\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".repositories." + entityName + "Repository;\n");
            writer.write("import jakarta.persistence.EntityNotFoundException;\n");
            writer.write("import org.springframework.stereotype.Service;\n\n");
            writer.write("import java.util.List;\n");
            writer.write("import java.util.Optional;\n\n");

            writer.write("@Service\n");
            writer.write("public class " + className + " {\n\n");

            // Add Repository Field (constructor injection)
            writer.write("    private final " + entityName + "Repository " + entityName.toLowerCase() + "Repository;\n\n");

            // Constructor for injection
            writer.write("    public " + className + "(" + entityName + "Repository " + entityName.toLowerCase() + "Repository) {\n");
            writer.write("        this." + entityName.toLowerCase() + "Repository = " + entityName.toLowerCase() + "Repository;\n");
            writer.write("    }\n\n");

            // Get All Method
            writer.write("    public List<" + entityName + "> getAll" + entityName + "s() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.findAll();\n");
            writer.write("    }\n\n");

            // Get All DisplayInList = true Method
            writer.write("    public List<" + entityName + "> getAllDisplayInListTrue() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.findByDisplayInListTrue();\n");
            writer.write("    }\n\n");

            // Create Method
            writer.write("    public " + entityName + " create" + entityName + "(" + entityName + " " + entityName.toLowerCase() + ") {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.save(" + entityName.toLowerCase() + ");\n");
            writer.write("    }\n\n");

            // Get by ID Method
            writer.write("    public Optional<" + entityName + "> get" + entityName + "ById(Long id) {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.findById(id);\n");
            writer.write("    }\n\n");

            // Update Method
            writer.write("    public " + entityName + " update" + entityName + "(Long id, " + entityName + " " + entityName.toLowerCase() + "Details) {\n");
            writer.write("        Optional<" + entityName + "> optional" + entityName + " = get" + entityName + "ById(id);\n");
            writer.write("        if (optional" + entityName + ".isPresent()) {\n");
            writer.write("            " + entityName + " " + entityName.toLowerCase() + " = optional" + entityName + ".get();\n");
            for (AttributeDTO attribute : attributes) {
                writer.write("            " + entityName.toLowerCase() + ".set" + capitalize(attribute.getAttributeName()) + "(" + entityName.toLowerCase() + "Details.get" + capitalize(attribute.getAttributeName()) + "());\n");
            }
            writer.write("            return " + entityName.toLowerCase() + "Repository.save(" + entityName.toLowerCase() + ");\n");
            writer.write("        }\n");
            writer.write("        throw new EntityNotFoundException(\"Entity not found\");\n");
            writer.write("    }\n\n");

            // Delete Method
            writer.write("    public void delete" + entityName + "(Long id) {\n");
            writer.write("        " + entityName.toLowerCase() + "Repository.deleteById(id);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    public void generateControllerClass(String projectName, String entityName) throws IOException {
        String className = entityName + "Controller";
        String fileName = projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/controllers/" + className + ".java";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("package com.example." + projectName.toLowerCase() + ".controllers;\n\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".services." + entityName + "Service;\n");
            writer.write("import org.springframework.http.ResponseEntity;\n");
            writer.write("import jakarta.persistence.EntityNotFoundException;\n");
            writer.write("import org.springframework.web.bind.annotation.*;\n\n");
            writer.write("import java.util.List;\n");
            writer.write("import java.util.Optional;\n\n");

            writer.write("@RestController\n");
            writer.write("@RequestMapping(\"/api/" + entityName.toLowerCase() + "s\")\n");
            writer.write("public class " + className + " {\n\n");

            // Constructor-based Dependency Injection
            writer.write("    private final " + entityName + "Service " + entityName.toLowerCase() + "Service;\n\n");

            writer.write("    public " + className + "(" + entityName + "Service " + entityName.toLowerCase() + "Service) {\n");
            writer.write("        this." + entityName.toLowerCase() + "Service = " + entityName.toLowerCase() + "Service;\n");
            writer.write("    }\n\n");

            // Get All Method
            writer.write("    @GetMapping\n");
            writer.write("    public List<" + entityName + "> getAll" + entityName + "s() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Service.getAll" + entityName + "s();\n");
            writer.write("    }\n\n");

            // Get All DisplayInList = true Method
            writer.write("    @GetMapping(\"/display-in-list-true\")\n");
            writer.write("    public List<" + entityName + "> getAllDisplayInListTrue() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Service.getAllDisplayInListTrue();\n");
            writer.write("    }\n\n");

            // Create Method
            writer.write("    @PostMapping\n");
            writer.write("    public " + entityName + " create" + entityName + "(@RequestBody " + entityName + " " + entityName.toLowerCase() + ") {\n");
            writer.write("        return " + entityName.toLowerCase() + "Service.create" + entityName + "(" + entityName.toLowerCase() + ");\n");
            writer.write("    }\n\n");

            // Get by ID Method
            writer.write("    @GetMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<" + entityName + "> get" + entityName + "ById(@PathVariable Long id) {\n");
            writer.write("        Optional<" + entityName + "> optional" + entityName + " = " + entityName.toLowerCase() + "Service.get" + entityName + "ById(id);\n");
            writer.write("        return optional" + entityName + ".map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());\n");
            writer.write("    }\n\n");

            // Update Method
            writer.write("    @PutMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<" + entityName + "> update" + entityName + "(@PathVariable Long id, @RequestBody " + entityName + " " + entityName.toLowerCase() + "Details) {\n");
            writer.write("        try {\n");
            writer.write("            " + entityName + " updated" + entityName + " = " + entityName.toLowerCase() + "Service.update" + entityName + "(id, " + entityName.toLowerCase() + "Details);\n");
            writer.write("            return ResponseEntity.ok(updated" + entityName + ");\n");
            writer.write("        } catch (EntityNotFoundException e) {\n");
            writer.write("            return ResponseEntity.notFound().build();\n");
            writer.write("        }\n");
            writer.write("    }\n\n");

            // Delete Method
            writer.write("    @DeleteMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<Void> delete" + entityName + "(@PathVariable Long id) {\n");
            writer.write("        " + entityName.toLowerCase() + "Service.delete" + entityName + "(id);\n");
            writer.write("        return ResponseEntity.noContent().build();\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

}
