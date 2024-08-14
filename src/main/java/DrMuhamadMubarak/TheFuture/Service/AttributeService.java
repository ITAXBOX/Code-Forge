package DrMuhamadMubarak.TheFuture.Service;

import DrMuhamadMubarak.TheFuture.DTO.AttributeDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AttributeService {

    private final Map<String, Map<String, List<AttributeDTO>>> projectEntityAttributesMap = new HashMap<>();

    public void addAttributesToEntity(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        // Initialize project and entity maps
        Map<String, List<AttributeDTO>> entityAttributesMap = projectEntityAttributesMap
                .computeIfAbsent(projectName, _ -> new HashMap<>());

        // Clear existing attributes for the entity before adding new ones
        entityAttributesMap.put(entityName, new ArrayList<>(attributes));

        // Generate or update the entity class file
        generateOrUpdateEntityClass(projectName, entityName);
    }

    private void generateOrUpdateEntityClass(String projectName, String entityName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models";
        Path entityFilePath = Paths.get(baseDir, entityName + ".java");

        if (Files.notExists(entityFilePath)) {
            String initialContent = "package com.example." + projectName.toLowerCase() + ".models;\n\npublic class " + entityName + " {\n}";
            Files.writeString(entityFilePath, initialContent);
        }

        String entityContent = Files.readString(entityFilePath);
        StringBuilder updatedContent = new StringBuilder(entityContent);

        // Append attributes before the last closing brace of the class
        int classEndIndex = entityContent.lastIndexOf("}");
        if (classEndIndex == -1) {
            throw new IllegalStateException("Class end not found in file: " + entityFilePath);
        }

        // Generate new fields for attributes
        List<AttributeDTO> attributes = getAttributesForEntity(projectName, entityName);
        for (AttributeDTO attribute : attributes) {
            updatedContent.insert(classEndIndex, generateFieldForAttribute(attribute));
        }

        Files.writeString(entityFilePath, updatedContent.toString());
    }

    private String generateFieldForAttribute(AttributeDTO attribute) {
        StringBuilder field = new StringBuilder();

        // Add primary key annotation if applicable
        if (attribute.isPrimaryKey()) {
            field.append("\n    @Id");
        }

        // Add column annotation if applicable
        if (attribute.getDataType() != null) {
            field.append("\n    @Column(");
            boolean hasLength = attribute.getDataSize() != null && !attribute.getDataSize().isEmpty();
            boolean isNullable = attribute.isNullable(); // Check if nullable is explicitly set to true
            boolean hasDefaultValue = attribute.getDefaultValue() != null;

            // Only include attributes if they are present
            if (hasLength) {
                field.append("length = ").append(attribute.getDataSize());
            }

            if (!isNullable) { // Add nullable = false only if explicitly set to false
                if (hasLength) {
                    field.append(", ");
                }
                field.append("nullable = false");
            }

            if (hasDefaultValue) {
                if (hasLength || !isNullable) {
                    field.append(", ");
                }
                field.append("columnDefinition = \"").append(attribute.getDataType().name().toLowerCase()).append(" default '").append(attribute.getDefaultValue()).append("'\"");
            }

            field.append(")");
            field.append("\n    private ").append(formatDataType(attribute.getDataType())).append(" ").append(attribute.getAttributeName()).append(";");
        }

        // Add relationship annotation if applicable
        if (attribute.getRelationshipType() != null && !attribute.getRelationshipType().equals(AttributeDTO.RelationshipType.NONE)) {
            field.append(generateRelationshipAnnotation(attribute));
        }

        // Add display in list comment if applicable
        if (attribute.isDisplayInList()) {
            field.append("\n    // This attribute is marked for display in lists");
        }

        field.append("\n");
        return field.toString();
    }

    private String formatDataType(AttributeDTO.DataType dataType) {
        return switch (dataType) {
            case STRING -> "String";
            case INTEGER -> "int";
            case DATE -> "Instant";
            default -> dataType.name().toLowerCase();
        };
    }

    private String generateRelationshipAnnotation(AttributeDTO attribute) {
        String relationshipAnnotation = switch (attribute.getRelationshipType()) {
            case MANY_TO_ONE -> "@ManyToOne(fetch = FetchType.LAZY)";
            case ONE_TO_MANY ->
                    "@OneToMany(mappedBy = \"" + attribute.getAttributeName() + "\", fetch = FetchType.LAZY)";
            case ONE_TO_ONE -> "@OneToOne(fetch = FetchType.LAZY)";
            case MANY_TO_MANY -> "@ManyToMany(fetch = FetchType.LAZY)";
            default -> "";
        };

        StringBuilder relationship = new StringBuilder();
        if (!relationshipAnnotation.isEmpty()) {
            relationship.append("\n    ").append(relationshipAnnotation);
            if (attribute.getReferenceEntity() != null) {
                relationship.append("\n    private ").append(attribute.getReferenceEntity()).append(" ").append(attribute.getAttributeName()).append(";");
            }
        }

        return relationship.toString();
    }

    public List<AttributeDTO> getAttributesForEntity(String projectName, String entityName) {
        return projectEntityAttributesMap.getOrDefault(projectName, new HashMap<>()).getOrDefault(entityName, new ArrayList<>());
    }
}
