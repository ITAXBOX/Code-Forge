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

    public void generateEntityUI(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String baseDir = projectName + "/src/main/resources/static";
        String fileName = baseDir + "/" + entityName.toLowerCase() + ".html";

        // Create directories if they do not exist
        Files.createDirectories(Paths.get(baseDir));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Basic HTML structure with DataTables styles and scripts
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"en\">\n");
            writer.write("<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("    <title>" + entityName + " List</title>\n");

            // Include DataTables CSS
            writer.write("    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.13.4/css/jquery.dataTables.min.css\">\n");

            // Add custom styles
            writer.write("    <style>\n");
            writer.write("        body {\n");
            writer.write("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
            writer.write("            background-color: #f0f4f8;\n");
            writer.write("            margin: 0;\n");
            writer.write("            padding: 0;\n");
            writer.write("            display: flex;\n");
            writer.write("            justify-content: center;\n");
            writer.write("            align-items: center;\n");
            writer.write("            min-height: 100vh;\n");
            writer.write("        }\n");
            writer.write("        .container {\n");
            writer.write("            width: 90%;\n");
            writer.write("            max-width: 1200px;\n");
            writer.write("            padding: 20px;\n");
            writer.write("            background-color: #fff;\n");
            writer.write("            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);\n");
            writer.write("            border-radius: 8px;\n");
            writer.write("        }\n");
            writer.write("        h1 {\n");
            writer.write("            text-align: center;\n");
            writer.write("            color: #333;\n");
            writer.write("            margin-bottom: 30px;\n");
            writer.write("            font-size: 2.5rem;\n");
            writer.write("        }\n");
            writer.write("        .controls {\n");
            writer.write("            margin-bottom: 20px;\n");
            writer.write("            text-align: center;\n");
            writer.write("        }\n");
            writer.write("        .controls label {\n");
            writer.write("            margin-right: 10px;\n");
            writer.write("        }\n");
            writer.write("        td.clickable-id {\n");
            writer.write("            cursor: pointer;\n");
            writer.write("            color: #007bff;\n");
            writer.write("            text-decoration: underline;\n");
            writer.write("        }\n");
            writer.write("        td.clickable-id:hover {\n");
            writer.write("            color: #0056b3;\n");
            writer.write("        }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");

            writer.write("<body>\n");

            writer.write("    <div class=\"container\">\n");
            writer.write("    <h1>" + entityName + " List</h1>\n");

            // Add checkboxes for column visibility control
            writer.write("    <div class=\"controls\">\n");
            for (int i = 0; i < attributes.size(); i++) {
                writer.write("        <label><input type=\"checkbox\" class=\"toggle-column\" data-column=\"" + i + "\" checked> "
                             + capitalize(attributes.get(i).getAttributeName()) + "</label>\n");
            }
            writer.write("    </div>\n");

            // Table structure
            writer.write("    <table id=\"entity-table\" class=\"display\">\n");
            writer.write("        <thead>\n");
            writer.write("            <tr>\n");
            for (AttributeDTO attribute : attributes) {
                writer.write("                <th>" + capitalize(attribute.getAttributeName()) + "</th>\n");
            }
            writer.write("            </tr>\n");
            writer.write("        </thead>\n");
            writer.write("        <tbody>\n");
            writer.write("            <!-- Rows will be dynamically inserted here -->\n");
            writer.write("        </tbody>\n");
            writer.write("    </table>\n");
            writer.write("    </div>\n");

            // Include jQuery and DataTables scripts
            writer.write("    <script src=\"https://code.jquery.com/jquery-3.6.0.min.js\"></script>\n");
            writer.write("    <script src=\"https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js\"></script>\n");

            // JavaScript for fetching data and initializing DataTables
            writer.write("    <script>\n");
            writer.write("        document.addEventListener('DOMContentLoaded', function() {\n");
            writer.write("            fetch('/api/" + entityName.toLowerCase() + "s')\n");
            writer.write("                .then(response => response.json())\n");
            writer.write("                .then(data => {\n");
            writer.write("                    const tableBody = document.querySelector('#entity-table tbody');\n");
            writer.write("                    tableBody.innerHTML = '';\n");
            writer.write("                    data.forEach(entity => {\n");
            writer.write("                        const row = document.createElement('tr');\n");
            for (AttributeDTO attribute : attributes) {
                if ("id".equalsIgnoreCase(attribute.getAttributeName())) {
                    writer.write("                        const cell_id = document.createElement('td');\n");
                    writer.write("                        const link = document.createElement('a');\n");
                    writer.write("                        link.textContent = entity." + attribute.getAttributeName() + ";\n");
                    writer.write("                        link.href = '" + entityName.toLowerCase() + "-get-by-id.html?id=' + entity." + attribute.getAttributeName() + ";\n");
                    writer.write("                        cell_id.appendChild(link);\n");
                    writer.write("                        row.appendChild(cell_id);\n");
                } else {
                    writer.write("                        const cell_" + attribute.getAttributeName() + " = document.createElement('td');\n");
                    writer.write("                        cell_" + attribute.getAttributeName() + ".textContent = entity." + attribute.getAttributeName() + ";\n");
                    writer.write("                        row.appendChild(cell_" + attribute.getAttributeName() + ");\n");
                }
            }
            writer.write("                        tableBody.appendChild(row);\n");
            writer.write("                    });\n");

            // Initialize DataTables
            writer.write("                    const table = $('#entity-table').DataTable();\n");

            // Add event listeners for column visibility toggling
            writer.write("                    document.querySelectorAll('.toggle-column').forEach(checkbox => {\n");
            writer.write("                        checkbox.addEventListener('change', function() {\n");
            writer.write("                            const column = table.column($(this).data('column'));\n");
            writer.write("                            column.visible(this.checked);\n");
            writer.write("                        });\n");
            writer.write("                    });\n");

            writer.write("                })\n");
            writer.write("                .catch(error => console.error('Error:', error));\n");
            writer.write("        });\n");
            writer.write("    </script>\n");

            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }

    public void generateEntityGetByIdUI(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String baseDir = projectName + "/src/main/resources/static";
        String fileName = baseDir + "/" + entityName.toLowerCase() + "-get-by-id.html";

        // Create directories if they do not exist
        Files.createDirectories(Paths.get(baseDir));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Basic HTML structure
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"en\">\n");
            writer.write("<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("    <title>Get " + entityName + " by ID</title>\n");
            writer.write("    <style>\n");
            writer.write("        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }\n");
            writer.write("        .container { max-width: 600px; margin: auto; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }\n");
            writer.write("        h1 { text-align: center; color: #333; }\n");
            writer.write("        label { display: block; margin-bottom: 8px; color: #555; }\n");
            writer.write("        input[type='number'] { width: calc(100% - 22px); padding: 10px; margin-bottom: 10px; border: 1px solid #ccc; border-radius: 4px; }\n");
            writer.write("        button { width: 100%; padding: 10px; background-color: #007BFF; color: white; border: none; border-radius: 4px; cursor: pointer; }\n");
            writer.write("        button:hover { background-color: #0056b3; }\n");
            writer.write("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
            writer.write("        table, th, td { border: 1px solid #ddd; }\n");
            writer.write("        th, td { padding: 12px; text-align: left; }\n");
            writer.write("        th { background-color: #f2f2f2; }\n");
            writer.write("        tr:hover { background-color: #f1f1f1; }\n");
            writer.write("        @media (max-width: 600px) { \n");
            writer.write("            table, th, td { display: block; }\n");
            writer.write("            tr { margin-bottom: 10px; }\n");
            writer.write("        }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");

            writer.write("    <div class=\"container\">\n");
            writer.write("        <h1>Get " + entityName + " by ID</h1>\n");

            // Input field for the ID
            writer.write("        <label for=\"entity-id\">Enter " + entityName + " ID:</label>\n");
            writer.write("        <input type=\"number\" id=\"entity-id\" placeholder=\"Enter ID\" />\n");
            writer.write("        <button id=\"fetch-button\">Fetch " + entityName + "</button>\n");

            // Table structure to display the entity data
            writer.write("        <table id=\"entity-table\">\n");
            writer.write("            <thead>\n");
            writer.write("                <tr>\n");

            // Dynamically create headers based on attributes
            for (AttributeDTO attribute : attributes) {
                writer.write("                    <th>" + capitalize(attribute.getAttributeName()) + "</th>\n");
            }

            writer.write("                </tr>\n");
            writer.write("            </thead>\n");
            writer.write("            <tbody>\n");
            writer.write("                <!-- Rows will be dynamically inserted here -->\n");
            writer.write("            </tbody>\n");
            writer.write("        </table>\n");

            // JavaScript to fetch entity by ID
            writer.write("        <script>\n");
            writer.write("            document.addEventListener('DOMContentLoaded', function() {\n");
            writer.write("                const params = new URLSearchParams(window.location.search);\n");
            writer.write("                const id = params.get('id');\n");
            writer.write("                if (id) {\n");
            writer.write("                    document.getElementById('entity-id').value = id;\n");
            writer.write("                    fetchEntityById(id);\n");
            writer.write("                }\n");
            writer.write("            });\n");

            writer.write("            document.getElementById('fetch-button').addEventListener('click', function() {\n");
            writer.write("                const id = document.getElementById('entity-id').value;\n");
            writer.write("                if (!id) {\n");
            writer.write("                    alert('Please enter an ID.');\n");
            writer.write("                    return;\n");
            writer.write("                }\n");
            writer.write("                fetchEntityById(id);\n");
            writer.write("            });\n");

            writer.write("            function fetchEntityById(id) {\n");
            writer.write("                fetch('/api/" + entityName.toLowerCase() + "s/' + id)\n");
            writer.write("                    .then(response => {\n");
            writer.write("                        if (!response.ok) {\n");
            writer.write("                            throw new Error('Entity not found');\n");
            writer.write("                        }\n");
            writer.write("                        return response.json();\n");
            writer.write("                    })\n");
            writer.write("                    .then(data => {\n");
            writer.write("                        const tableBody = document.querySelector('#entity-table tbody');\n");
            writer.write("                        tableBody.innerHTML = '';\n");
            writer.write("                        const row = document.createElement('tr');\n");

            // Dynamically populate the table rows with entity data
            for (AttributeDTO attribute : attributes) {
                writer.write("                        const " + attribute.getAttributeName() + "Cell = document.createElement('td');\n");
                writer.write("                        " + attribute.getAttributeName() + "Cell.textContent = data." + attribute.getAttributeName() + " !== undefined ? data." + attribute.getAttributeName() + " : 'N/A';\n");
                writer.write("                        row.appendChild(" + attribute.getAttributeName() + "Cell);\n");
            }

            writer.write("                        tableBody.appendChild(row);\n");
            writer.write("                    })\n");
            writer.write("                    .catch(error => {\n");
            writer.write("                        alert('Error: ' + error.message);\n");
            writer.write("                    });\n");
            writer.write("            }\n");
            writer.write("        </script>\n");

            writer.write("    </div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
}
