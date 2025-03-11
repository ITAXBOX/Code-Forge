package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;

@Service
@AllArgsConstructor
public class ProjectAttributeGenerationService {

    private final ProjectEntitiesService projectEntitiesService;
    private final ProjectAttributesService projectAttributesService;

    public String saveAttributes(
            String projectName,
            String entityName,
            String action,
            AttributeDTO attribute,
            Model model) throws IOException {

        projectAttributesService.addAttributesToEntity(projectName, entityName, attribute);
        model.addAttribute("projectName", projectName);

        if ("next".equals(action)) {
            return handleNextAction(projectName, entityName, model);
        } else {
            model.addAttribute("entityName", entityName);
            return "add-attributes";
        }
    }

    private String handleNextAction(String projectName, String entityName, Model model) throws IOException {
        projectEntitiesService.generateServiceClass(projectName, entityName, projectAttributesService.getAttributes());
        projectEntitiesService.generateControllerClass(projectName, entityName);
        projectEntitiesService.generateUI(projectName, entityName, projectAttributesService.getAttributes());

        projectAttributesService.clearAttributes();

        if (projectEntitiesService.isLastEntity(entityName)) {
            projectEntitiesService.generateSecurityClass(projectName);
            projectEntitiesService.generateDataInitializerClass(projectName);
            projectEntitiesService.generateAuthenticationControllerClass(projectName);

            model.addAttribute("message", "Project generated successfully");
            return "result";
        } else {
            model.addAttribute("entityName", projectEntitiesService.getNextEntityName(entityName));
            return "add-attributes";
        }
    }
}