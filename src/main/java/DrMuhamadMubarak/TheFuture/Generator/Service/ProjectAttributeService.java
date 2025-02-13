package DrMuhamadMubarak.TheFuture.Generator.Service;

import DrMuhamadMubarak.TheFuture.Generator.DTO.AttributeDTO;
import DrMuhamadMubarak.TheFuture.SpringBoot.SpringEntitiesAttributes;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class ProjectAttributeService {
    public void addAttributesToEntity(String projectName, String entityName, AttributeDTO attribute) throws IOException {
        SpringEntitiesAttributes.generateOrUpdateSpringEntityClass(projectName, entityName, attribute);
    }
}
