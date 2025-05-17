package DrMuhamadMubarak.TheFuture.generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelationshipValidator {
    public String validateAndFixRelationships(String entitiesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(entitiesJson);

            if (!rootNode.has("entities") || !rootNode.get("entities").isArray()) {
                return entitiesJson; // Invalid format, return original
            }

            // Create a copy of the entities array that we can modify
            ArrayNode entitiesNode = (ArrayNode) rootNode.get("entities");
            Map<String, ObjectNode> entityMap = new HashMap<>();

            // Map entity names to their full nodes for easy lookup
            for (JsonNode entityNode : entitiesNode) {
                if (entityNode.has("name")) {
                    String entityName = entityNode.get("name").asText();
                    entityMap.put(entityName, (ObjectNode) entityNode);
                }
            }

            // Process each entity to check relationships
            for (JsonNode entityNode : entitiesNode) {
                if (!entityNode.has("name")) {
                    continue; // Skip invalid entities
                }

                String entityName = entityNode.get("name").asText();

                // Ensure entity has an attributes array
                ArrayNode attributesNode;
                if (!entityNode.has("attributes") || !entityNode.get("attributes").isArray()) {
                    attributesNode = objectMapper.createArrayNode();
                    ((ObjectNode) entityNode).set("attributes", attributesNode);
                } else {
                    attributesNode = (ArrayNode) entityNode.get("attributes");
                }

                for (JsonNode attributeNode : attributesNode) {
                    if (attributeNode.has("relationshipType") && attributeNode.has("relatedEntity") && attributeNode.has("attributeName")) {
                        String relationshipType = attributeNode.get("relationshipType").asText();
                        String relatedEntityName = attributeNode.get("relatedEntity").asText();
                        String attributeName = attributeNode.get("attributeName").asText();

                        // Find the related entity
                        ObjectNode relatedEntityNode = entityMap.get(relatedEntityName);
                        if (relatedEntityNode != null) {
                            ensureComplementaryRelationship(
                                    relatedEntityNode,
                                    entityName,
                                    relatedEntityName,
                                    relationshipType,
                                    attributeName,
                                    objectMapper
                            );
                        }
                    }
                }
            }

            // Debug: Print entities after processing
            System.out.println("After processing, entities:");
            for (JsonNode entityNode : entitiesNode) {
                String name = entityNode.get("name").asText();
                System.out.println("Entity: " + name);
                if (entityNode.has("attributes") && entityNode.get("attributes").isArray()) {
                    System.out.println("  Attributes count: " + entityNode.get("attributes").size());
                    for (JsonNode attr : entityNode.get("attributes")) {
                        System.out.println("  - " + attr);
                    }
                } else {
                    System.out.println("  No attributes array");
                }
            }

            // Update the entities in the root node
            ((ObjectNode) rootNode).set("entities", entitiesNode);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        } catch (Exception e) {
            System.err.println("Error validating relationships: " + e.getMessage());
            return entitiesJson;
        }
    }

    private void ensureComplementaryRelationship(
            ObjectNode targetEntity,
            String sourceEntityName,
            String targetEntityName,
            String relationshipType,
            String sourceAttributeName,
            ObjectMapper objectMapper) {

        // Ensure target entity has an attributes array
        ArrayNode targetAttributes;
        if (!targetEntity.has("attributes") || !targetEntity.get("attributes").isArray()) {
            // Create a new attributes array if it doesn't exist
            targetAttributes = objectMapper.createArrayNode();
            targetEntity.set("attributes", targetAttributes);
        } else {
            targetAttributes = (ArrayNode) targetEntity.get("attributes");
        }

        // Check the relationship type and ensure the corresponding relationship exists
        switch (relationshipType) {
            case "ONE_TO_MANY":
                // For ONE_TO_MANY, ensure MANY_TO_ONE exists on the other side
                if (!hasRelationship(targetAttributes, "MANY_TO_ONE", sourceEntityName)) {
                    // Create complementary MANY_TO_ONE relationship
                    ObjectNode newAttribute = objectMapper.createObjectNode();
                    String attributeName = getAttributeNameFromEntityName(sourceEntityName);

                    newAttribute.put("attributeName", attributeName);
                    newAttribute.put("dataType", sourceEntityName);
                    newAttribute.put("relationshipType", "MANY_TO_ONE");
                    newAttribute.put("relatedEntity", sourceEntityName);
                    newAttribute.put("isNullable", true);
                    targetAttributes.add(newAttribute);

                    // Log fixed relationship but don't add to JSON
                    System.out.println("Fixed relationship: " + targetEntityName + "." + attributeName +
                            " (MANY_TO_ONE) -> " + sourceEntityName);
                }
                break;

            case "MANY_TO_ONE":
                // For MANY_TO_ONE, ensure ONE_TO_MANY exists on the target entity
                if (!hasRelationship(targetAttributes, "ONE_TO_MANY", sourceEntityName)) {
                    ObjectNode newAttribute = objectMapper.createObjectNode();
                    String pluralAttributeName = pluralize(getAttributeNameFromEntityName(sourceEntityName));

                    newAttribute.put("attributeName", pluralAttributeName);
                    newAttribute.put("dataType", "List<" + sourceEntityName + ">");
                    newAttribute.put("relationshipType", "ONE_TO_MANY");
                    newAttribute.put("relatedEntity", sourceEntityName);
                    newAttribute.put("mappedBy", sourceAttributeName);
                    newAttribute.put("isNullable", true);

                    // Add to target entity (Order in our test case)
                    targetAttributes.add(newAttribute);

                    System.out.println("Fixed relationship: " + targetEntityName + "." + pluralAttributeName +
                            " (ONE_TO_MANY) -> " + sourceEntityName);
                }
                break;

            case "MANY_TO_MANY":
                // For MANY_TO_MANY, ensure MANY_TO_MANY exists on the other side
                if (!hasRelationship(targetAttributes, "MANY_TO_MANY", sourceEntityName)) {
                    // Create complementary MANY_TO_MANY relationship
                    ObjectNode newAttribute = objectMapper.createObjectNode();
                    String pluralAttributeName = pluralize(getAttributeNameFromEntityName(sourceEntityName));

                    // Do not add comment field for auto-fixed relationships

                    newAttribute.put("attributeName", pluralAttributeName);
                    newAttribute.put("dataType", "Set<" + sourceEntityName + ">");
                    newAttribute.put("relationshipType", "MANY_TO_MANY");
                    newAttribute.put("relatedEntity", sourceEntityName);
                    newAttribute.put("mappedBy", sourceAttributeName);
                    newAttribute.put("isNullable", true);
                    targetAttributes.add(newAttribute);

                    // Log fixed relationship but don't add to JSON
                    System.out.println("Fixed relationship: " + targetEntityName + "." + pluralAttributeName +
                            " (MANY_TO_MANY) -> " + sourceEntityName);
                }
                break;

            case "ONE_TO_ONE":
                // For ONE_TO_ONE, ensure ONE_TO_ONE exists on the other side
                if (!hasRelationship(targetAttributes, "ONE_TO_ONE", sourceEntityName)) {
                    // Create complementary ONE_TO_ONE relationship
                    ObjectNode newAttribute = objectMapper.createObjectNode();
                    String attributeName = getAttributeNameFromEntityName(sourceEntityName);

                    // Do not add comment field for auto-fixed relationships

                    newAttribute.put("attributeName", attributeName);
                    newAttribute.put("dataType", sourceEntityName);
                    newAttribute.put("relationshipType", "ONE_TO_ONE");
                    newAttribute.put("relatedEntity", sourceEntityName);
                    newAttribute.put("mappedBy", sourceAttributeName);
                    newAttribute.put("isNullable", true);
                    targetAttributes.add(newAttribute);

                    // Log fixed relationship but don't add to JSON
                    System.out.println("Fixed relationship: " + targetEntityName + "." + attributeName +
                            " (ONE_TO_ONE) -> " + sourceEntityName);
                }
                break;
        }
    }

    private boolean hasRelationship(ArrayNode attributes, String relationshipType, String relatedEntityName) {
        for (JsonNode attribute : attributes) {
            if (attribute.has("relationshipType") &&
                    attribute.get("relationshipType").asText().equals(relationshipType) &&
                    attribute.has("relatedEntity") &&
                    attribute.get("relatedEntity").asText().equals(relatedEntityName)) {
                return true;
            }
        }
        return false;
    }

    private String getAttributeNameFromEntityName(String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            return "unknown";
        }
        // Convert entity name to camelCase (e.g., "Product" -> "product")
        return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
    }

    private String pluralize(String singularName) {
        if (singularName == null || singularName.isEmpty()) {
            return "items";
        }

        // Simple pluralization logic - can be enhanced for more complex rules
        if (singularName.endsWith("y")) {
            return singularName.substring(0, singularName.length() - 1) + "ies";
        } else if (singularName.endsWith("s") || singularName.endsWith("x") ||
                singularName.endsWith("z") || singularName.endsWith("ch") ||
                singularName.endsWith("sh")) {
            return singularName + "es";
        } else {
            return singularName + "s";
        }
    }
}
