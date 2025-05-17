package DrMuhamadMubarak.TheFuture.generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RelationshipValidatorTest {

    private RelationshipValidator relationshipValidator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        relationshipValidator = new RelationshipValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testOneToManyRelationshipFix() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Order",
                            "attributes": [
                                {
                                    "attributeName": "items",
                                    "dataType": "List<OrderItem>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "OrderItem"
                                }
                            ]
                        },
                        {
                            "name": "OrderItem",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Find the OrderItem entity and verify it has a MANY_TO_ONE relationship to Order
        JsonNode orderItemEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("OrderItem")) {
                orderItemEntity = entity;
                break;
            }
        }

        assertNotNull(orderItemEntity, "OrderItem entity not found");

        boolean hasExpectedRelationship = false;
        for (JsonNode attribute : orderItemEntity.get("attributes")) {
            if (attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("MANY_TO_ONE") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("Order")) {
                hasExpectedRelationship = true;
                break;
            }
        }

        assertTrue(hasExpectedRelationship, "MANY_TO_ONE relationship to Order not found in OrderItem");
    }

    @Test
    void testManyToOneRelationshipFix() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "OrderItem",
                            "attributes": [
                                {
                                    "attributeName": "order",
                                    "dataType": "Order",
                                    "relationshipType": "MANY_TO_ONE",
                                    "relatedEntity": "Order"
                                }
                            ]
                        },
                        {
                            "name": "Order",
                            "attributes": []
                        }
                    ]
                }
                """;

        System.out.println("Before validation JSON:");
        System.out.println(json);

        String result = relationshipValidator.validateAndFixRelationships(json);

        System.out.println("\nAfter validation JSON:");
        System.out.println(result);

        JsonNode resultNode = objectMapper.readTree(result);

        // Find the Order entity and verify it has a ONE_TO_MANY relationship to OrderItem
        JsonNode orderEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            System.out.println("Entity: " + entity.get("name").asText());
            if (entity.get("name").asText().equals("Order")) {
                orderEntity = entity;
                System.out.println("Found Order entity: " + orderEntity);
                break;
            }
        }

        assertNotNull(orderEntity, "Order entity not found");

        // Check attributes first
        JsonNode attributesNode = orderEntity.get("attributes");
        assertNotNull(attributesNode, "Attributes array not found in Order entity");

        // Debug: print all attributes in the Order entity
        System.out.println("Order attributes count: " + attributesNode.size());
        for (JsonNode attribute : attributesNode) {
            System.out.println("Attribute: " + attribute);
        }

        boolean hasExpectedRelationship = false;
        for (JsonNode attribute : attributesNode) {
            if (attribute.has("relationshipType")) {
                System.out.println("Found relationship: " + attribute.get("relationshipType").asText());

                if (attribute.get("relationshipType").asText().equals("ONE_TO_MANY") &&
                    attribute.has("relatedEntity") &&
                    attribute.get("relatedEntity").asText().equals("OrderItem")) {

                    System.out.println("Found matching relationship");
                    hasExpectedRelationship = true;
                    break;
                }
            }
        }

        assertTrue(hasExpectedRelationship, "ONE_TO_MANY relationship to OrderItem not found in Order");
    }

    @Test
    void testManyToManyRelationshipFix() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "User",
                            "attributes": [
                                {
                                    "attributeName": "roles",
                                    "dataType": "Set<Role>",
                                    "relationshipType": "MANY_TO_MANY",
                                    "relatedEntity": "Role"
                                }
                            ]
                        },
                        {
                            "name": "Role",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Find the Role entity and verify it has a MANY_TO_MANY relationship to User
        JsonNode roleEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Role")) {
                roleEntity = entity;
                break;
            }
        }

        assertNotNull(roleEntity, "Role entity not found");

        boolean hasExpectedRelationship = false;
        for (JsonNode attribute : roleEntity.get("attributes")) {
            if (attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("MANY_TO_MANY") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("User") &&
                attribute.has("mappedBy") &&
                attribute.get("mappedBy").asText().equals("roles")) {
                hasExpectedRelationship = true;
                break;
            }
        }

        assertTrue(hasExpectedRelationship, "MANY_TO_MANY relationship to User not found in Role");
    }

    @Test
    void testOneToOneRelationshipFix() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Customer",
                            "attributes": [
                                {
                                    "attributeName": "profile",
                                    "dataType": "CustomerProfile",
                                    "relationshipType": "ONE_TO_ONE",
                                    "relatedEntity": "CustomerProfile"
                                }
                            ]
                        },
                        {
                            "name": "CustomerProfile",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Find the CustomerProfile entity and verify it has a ONE_TO_ONE relationship to Customer
        JsonNode customerProfileEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("CustomerProfile")) {
                customerProfileEntity = entity;
                break;
            }
        }

        assertNotNull(customerProfileEntity, "CustomerProfile entity not found");

        boolean hasExpectedRelationship = false;
        for (JsonNode attribute : customerProfileEntity.get("attributes")) {
            if (attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("ONE_TO_ONE") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("Customer") &&
                attribute.has("mappedBy") &&
                attribute.get("mappedBy").asText().equals("profile")) {
                hasExpectedRelationship = true;
                break;
            }
        }

        assertTrue(hasExpectedRelationship, "ONE_TO_ONE relationship to Customer not found in CustomerProfile");
    }

    @Test
    void testNoChangesWhenRelationshipsExist() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Order",
                            "attributes": [
                                {
                                    "attributeName": "items",
                                    "dataType": "List<OrderItem>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "OrderItem",
                                    "mappedBy": "order"
                                }
                            ]
                        },
                        {
                            "name": "OrderItem",
                            "attributes": [
                                {
                                    "attributeName": "order",
                                    "dataType": "Order",
                                    "relationshipType": "MANY_TO_ONE",
                                    "relatedEntity": "Order"
                                }
                            ]
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        assertEquals(objectMapper.readTree(json), objectMapper.readTree(result));
    }

    @Test
    void testInvalidJsonReturnsOriginal() {
        String invalidJson = "invalid json";
        String result = relationshipValidator.validateAndFixRelationships(invalidJson);
        assertEquals(invalidJson, result);
    }

    @Test
    void testMissingEntitiesArrayReturnsOriginal() {
        String json = "{\"otherField\": \"value\"}";
        String result = relationshipValidator.validateAndFixRelationships(json);
        assertEquals(json, result);
    }

    @Test
    void testMultipleRelationshipsInOneEntity() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "User",
                            "attributes": [
                                {
                                    "attributeName": "orders",
                                    "dataType": "List<Order>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "Order"
                                },
                                {
                                    "attributeName": "roles",
                                    "dataType": "Set<Role>",
                                    "relationshipType": "MANY_TO_MANY",
                                    "relatedEntity": "Role"
                                }
                            ]
                        },
                        {
                            "name": "Order",
                            "attributes": []
                        },
                        {
                            "name": "Role",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Check Order got MANY_TO_ONE to User
        JsonNode orderEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Order")) {
                orderEntity = entity;
                break;
            }
        }
        assertNotNull(orderEntity, "Order entity not found");
        boolean hasManyToOneToUser = false;
        for (JsonNode attribute : orderEntity.get("attributes")) {
            if (attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("MANY_TO_ONE") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("User")) {
                hasManyToOneToUser = true;
                break;
            }
        }
        assertTrue(hasManyToOneToUser, "MANY_TO_ONE relationship to User not found in Order");

        // Check Role got MANY_TO_MANY to User
        JsonNode roleEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Role")) {
                roleEntity = entity;
                break;
            }
        }
        assertNotNull(roleEntity, "Role entity not found");
        boolean hasManyToManyToUser = false;
        for (JsonNode attribute : roleEntity.get("attributes")) {
            if (attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("MANY_TO_MANY") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("User")) {
                hasManyToManyToUser = true;
                break;
            }
        }
        assertTrue(hasManyToManyToUser, "MANY_TO_MANY relationship to User not found in Role");
    }

    @Test
    void testPluralizationLogic() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Library",
                            "attributes": [
                                {
                                    "attributeName": "books",
                                    "dataType": "List<Book>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "Book"
                                }
                            ]
                        },
                        {
                            "name": "Book",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Should create attribute "library" in Book (singular of Library)
        JsonNode bookEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Book")) {
                bookEntity = entity;
                break;
            }
        }
        assertNotNull(bookEntity, "Book entity not found");
        boolean hasLibraryAttribute = false;
        for (JsonNode attribute : bookEntity.get("attributes")) {
            if (attribute.has("attributeName") &&
                attribute.get("attributeName").asText().equals("library")) {
                hasLibraryAttribute = true;
                break;
            }
        }
        assertTrue(hasLibraryAttribute, "Attribute 'library' not found in Book");
    }

    @Test
    void testIrregularPluralization() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Person",
                            "attributes": [
                                {
                                    "attributeName": "children",
                                    "dataType": "List<Child>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "Child"
                                }
                            ]
                        },
                        {
                            "name": "Child",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Should handle "Person" -> "person" and "Child" -> "children" correctly
        JsonNode childEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Child")) {
                childEntity = entity;
                break;
            }
        }
        assertNotNull(childEntity, "Child entity not found");
        boolean hasPersonAttribute = false;
        for (JsonNode attribute : childEntity.get("attributes")) {
            if (attribute.has("attributeName") &&
                attribute.get("attributeName").asText().equals("person")) {
                hasPersonAttribute = true;
                break;
            }
        }
        assertTrue(hasPersonAttribute, "Attribute 'person' not found in Child");
    }

    @Test
    void testFixedRelationshipsTracking() throws Exception {
        String json = """
                {
                    "entities": [
                        {
                            "name": "Department",
                            "attributes": [
                                {
                                    "attributeName": "employees",
                                    "dataType": "List<Employee>",
                                    "relationshipType": "ONE_TO_MANY",
                                    "relatedEntity": "Employee"
                                }
                            ]
                        },
                        {
                            "name": "Employee",
                            "attributes": []
                        }
                    ]
                }
                """;

        String result = relationshipValidator.validateAndFixRelationships(json);
        JsonNode resultNode = objectMapper.readTree(result);

        // Verify the relationship was added
        JsonNode employeeEntity = null;
        for (JsonNode entity : resultNode.get("entities")) {
            if (entity.get("name").asText().equals("Employee")) {
                employeeEntity = entity;
                break;
            }
        }
        assertNotNull(employeeEntity, "Employee entity not found");
        boolean hasDepartmentAttribute = false;
        for (JsonNode attribute : employeeEntity.get("attributes")) {
            if (attribute.has("attributeName") &&
                attribute.get("attributeName").asText().equals("department") &&
                attribute.has("relationshipType") &&
                attribute.get("relationshipType").asText().equals("MANY_TO_ONE") &&
                attribute.has("relatedEntity") &&
                attribute.get("relatedEntity").asText().equals("Department")) {
                hasDepartmentAttribute = true;
                break;
            }
        }
        assertTrue(hasDepartmentAttribute, "Attribute 'department' with MANY_TO_ONE relationship to Department not found in Employee");
    }
}
