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

    public void generateEntityClasses(String projectName, String[] entities) throws IOException {
        SpringEntities.generateSpringEntityClasses(projectName, entities);
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

    public void generateUI(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        SpringUI.generateSpringUI(projectName, entityName, attributes);
    }
}
