package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.backend.springboot.*;
import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
@Service
public class EntityCodeGeneratorService {
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

    public void generateBehaviorServiceClass(String projectName, String entityName, String behaviorMethods) throws IOException {
        SpringService.generateSpringBehaviorServiceClass(projectName, entityName, behaviorMethods);
        SpringAIRepository.generateCompleteRepository(projectName, entityName, behaviorMethods);
    }

    public void generateAuthenticationServiceClass(String projectName) throws IOException {
        SpringService.generateAuthenticationServiceClass(projectName);
    }

    public void generateControllerClass(String projectName, String entityName) throws IOException {
        SpringController.generateControllerClass(projectName, entityName);
    }

    public void generateBehaviorControllerClass(String projectName, String entityName, String behaviorMethods) throws IOException {
        SpringBehaviorController.generateControllerClass(projectName, entityName, behaviorMethods);
    }

    public void generateAuthenticationControllerClass(String projectName) throws IOException {
        SpringController.generateAuthenticationControllerClass(projectName);
    }

    public void generateSecurityClass(String projectName) {
        SpringSecurity.generateSecurityClass(projectName);
    }

    public void generateDataInitializerClass(String projectName) {
        SpringDataInitializer.generateDataInitializerClass(projectName);
    }

    public void generateUtils(String projectName) throws IOException {
        SpringUtils.generateUtils(projectName);
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

    public List<String> getEntitiesAsList() {
        return List.of(getEntities());
    }
}