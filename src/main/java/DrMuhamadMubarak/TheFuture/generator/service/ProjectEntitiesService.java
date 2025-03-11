package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.generator.ui.ProjectUI;
import DrMuhamadMubarak.TheFuture.springboot.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
@Service
public class ProjectEntitiesService {
    private String[] entities;

    public String[] processEntities(String entitiesParam) throws IllegalArgumentException {
        if (entitiesParam == null || entitiesParam.trim().isEmpty()) {
            throw new IllegalArgumentException("No entities provided.");
        }
        this.entities = entitiesParam.split("\\s*,\\s*"); // Trim whitespace around commas
        return this.entities;
    }

    public void generateEntityClasses(String projectName, String[] entities) throws IOException {
        for (String entity : entities) {
            generateEntityClass(projectName, entity);
        }
    }

    public void generateEntityClass(String projectName, String entityName) throws IOException {
        SpringEntities.generateSpringEntityClass(projectName, entityName);
    }

    public void generateRepositoryClasses(String projectName, String[] entities) throws IOException {
        for (String entity : entities) {
            SpringRepository.generateSpringRepositoryClass(projectName, entity);
        }
    }

    public void generateRepositoryClass(String projectName, String entityName) throws IOException {
        SpringRepository.generateSpringRepositoryClass(projectName, entityName);
    }

    public void generateServiceClass(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        SpringService.generateSpringServiceClass(projectName, entityName, attributes);
    }

    public void generateControllerClass(String projectName, String entityName) throws IOException {
        SpringController.generateControllerClass(projectName, entityName);
    }

    public void generateSecurityClass(String projectName) {
        SpringSecurity.generateSecurityClass(projectName);
    }

    public void generateDataInitializerClass(String projectName) {
        SpringDataInitializer.generateDataInitializerClass(projectName);
    }

    public void generateUI(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        ProjectUI.generateProjectUI(projectName, entityName, attributes);
    }

    public boolean isLastEntity(String entityName) {
        String[] entities = getEntities();
        return entities != null && entities.length > 0 && entityName.equals(entities[entities.length - 1]);
    }

    public String getNextEntityName(String currentEntityName) {
        String[] entities = getEntities();
        if (entities != null && entities.length > 0) {
            for (int i = 0; i < entities.length - 1; i++) {
                if (entities[i].equals(currentEntityName)) {
                    return entities[i + 1];
                }
            }
        }
        return null;
    }
}