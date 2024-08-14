package DrMuhamadMubarak.TheFuture.Controller;

import DrMuhamadMubarak.TheFuture.DTO.AttributeDTO;
import DrMuhamadMubarak.TheFuture.Service.AttributeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AttributeController {

    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @GetMapping("/add-attributes")
    public String addAttributes(@RequestParam("projectName") String projectName,
                                @RequestParam("entityName") String entityName,
                                Model model) {
        // Initialize the attributes list for the entity
        model.addAttribute("projectName", projectName);
        model.addAttribute("entityName", entityName);
        model.addAttribute("attributes", new ArrayList<>()); // Initialize attributes list

        return "add-attributes";
    }

    @PostMapping("/save-attributes")
    public String saveAttributes(
            @RequestParam("projectName") String projectName,
            @RequestParam("entityName") String entityName,
            @RequestParam("action") String action,
            @RequestParam(value = "attributeName", required = false) String attributeName,
            @RequestParam(value = "dataType", required = false) String dataTypeStr,
            @RequestParam(value = "dataSize", required = false) String dataSize,
            @RequestParam(value = "defaultValue", required = false) String defaultValue,
            @RequestParam(value = "referenceEntity", required = false) String referenceEntity,
            @RequestParam(value = "isPrimaryKey", required = false) Boolean isPrimaryKey,
            @RequestParam(value = "isNullable", required = false) Boolean isNullable,
            @RequestParam(value = "displayInList", required = false) Boolean displayInList,
            @RequestParam(value = "relationshipType", required = false) String relationshipTypeStr,
            Model model) {

        try {
            // Convert strings to appropriate enum types
            AttributeDTO.DataType dataType = convertToDataType(dataTypeStr);
            AttributeDTO.RelationshipType relationshipType = convertToRelationshipType(relationshipTypeStr);

            // Create an AttributeDTO object
            AttributeDTO attribute = new AttributeDTO(attributeName, dataType, dataSize, defaultValue, referenceEntity, isPrimaryKey, isNullable, displayInList, relationshipType);

            // Add attribute and update the entity class
            attributeService.addAttributesToEntity(projectName, entityName, List.of(attribute));

            if ("next".equals(action)) {
                return "redirect:/add-attributes?projectName=" + projectName + "&entityName=" + getNextEntityName(projectName);
            } else if ("cancel".equals(action)) {
                return "redirect:/";  // Redirect to the main page or another appropriate URL
            } else {
                // Reset attributes list in the model for a new addition
                model.addAttribute("message", "Attribute saved successfully. Add another attribute or proceed.");
                model.addAttribute("projectName", projectName);
                model.addAttribute("entityName", entityName);
                model.addAttribute("attributes", new ArrayList<>()); // Reset attributes list for new entry
                return "add-attributes";
            }
        } catch (IOException e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    private AttributeDTO.DataType convertToDataType(String dataTypeStr) {
        try {
            return AttributeDTO.DataType.valueOf(dataTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;  // Handle invalid data type string as needed
        }
    }

    private AttributeDTO.RelationshipType convertToRelationshipType(String relationshipTypeStr) {
        try {
            return AttributeDTO.RelationshipType.valueOf(relationshipTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;  // Handle invalid relationship type string as needed
        }
    }

    private String getNextEntityName(String projectName) {
        // Implement logic to get the next entity name based on the project
        // For simplicity, returning a placeholder here
        return "NextEntity";
    }
}
