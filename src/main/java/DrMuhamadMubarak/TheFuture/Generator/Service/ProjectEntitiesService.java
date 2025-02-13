package DrMuhamadMubarak.TheFuture.Generator.Service;

import DrMuhamadMubarak.TheFuture.Generator.DTO.AttributeDTO;
import DrMuhamadMubarak.TheFuture.SpringBoot.*;
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
        SpringEntities.generateSpringEntityClasses(projectName, entities);
    }

    public void generateRepositoryClasses(String projectName, String[] entities) throws IOException {
        for (String entity : entities) {
            SpringRepository.generateSpringRepositoryClass(projectName, entity);
        }
    }

    public void generateServiceClass(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        SpringService.generateSpringServiceClass(projectName, entityName, attributes);
    }

    public void generateControllerClass(String projectName, String entityName) throws IOException {
        SpringController.generateControllerClass(projectName, entityName);
    }

    public void generateUI(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        SpringUI.generateSpringUI(projectName, entityName, attributes);
    }
}