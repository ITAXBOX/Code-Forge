package DrMuhamadMubarak.TheFuture.springboot;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static DrMuhamadMubarak.TheFuture.utils.Utils.capitalize;

public class SpringService {
    public static void generateSpringServiceClass(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
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
}
