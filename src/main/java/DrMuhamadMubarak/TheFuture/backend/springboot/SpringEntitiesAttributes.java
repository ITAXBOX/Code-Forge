package DrMuhamadMubarak.TheFuture.backend.springboot;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static DrMuhamadMubarak.TheFuture.backend.springboot.SpringEntityAttributesUtils.*;

public class SpringEntitiesAttributes {

    public static void generateOrUpdateSpringEntityClass(String projectName, String entityName, AttributeDTO attribute) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models";
        Path entityFilePath = Paths.get(baseDir, entityName + ".java");

        if (!Files.exists(entityFilePath)) {
            throw new IOException("File not found: " + entityFilePath);
        }

        String entityContent = Files.readString(entityFilePath);
        StringBuilder updatedContent = new StringBuilder(entityContent);

        int classEndIndex = entityContent.lastIndexOf("}");
        if (classEndIndex == -1) {
            throw new IllegalStateException("Class end not found in file: " + entityFilePath);
        }

        String attributeField = generateFieldForAttribute(attribute, entityName);
        updatedContent.insert(classEndIndex, attributeField);

        Files.writeString(entityFilePath, updatedContent.toString());
    }
}
