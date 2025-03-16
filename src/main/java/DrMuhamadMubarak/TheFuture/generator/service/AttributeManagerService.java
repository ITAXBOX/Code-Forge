package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AttributeManagerService {

    private final EntityCodeGeneratorService entityCodeGeneratorService;
    private final AttributeStorageService attributeStorageService;

    public String saveAttributes(
            String projectName,
            String entityName,
            String action,
            AttributeDTO attribute,
            Model model) throws IOException {

        attributeStorageService.addAttributesToEntity(projectName, entityName, attribute);
        model.addAttribute("projectName", projectName);

        if ("next".equals(action)) {
            return handleNextAction(projectName, entityName, model);
        } else {
            model.addAttribute("entityName", entityName);
            return "add-attributes";
        }
    }

    private String handleNextAction(String projectName, String entityName, Model model) throws IOException {
        entityCodeGeneratorService.generateServiceClass(projectName, entityName, attributeStorageService.getAttributes());
        entityCodeGeneratorService.generateControllerClass(projectName, entityName);

        attributeStorageService.clearAttributes();

        if (entityCodeGeneratorService.isLastEntity(entityName)) {
            entityCodeGeneratorService.generateSecurityClass(projectName);
            entityCodeGeneratorService.generateDataInitializerClass(projectName);

            entityCodeGeneratorService.generateAuthenticationServiceClass(projectName);
            entityCodeGeneratorService.generateAuthenticationControllerClass(projectName);

            entityCodeGeneratorService.generateUtils(projectName);

            model.addAttribute("message", "Project Generated Successfully.");
            return "result";
        } else {
            model.addAttribute("entityName", entityCodeGeneratorService.getNextEntityName(entityName));
            return "add-attributes";
        }
    }
}