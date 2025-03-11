package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import DrMuhamadMubarak.TheFuture.springboot.SpringEntitiesAttributes;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectAttributesService {

    private final List<AttributeDTO> attributes = new ArrayList<>();

    public void addAttributesToEntity(String projectName, String entityName, AttributeDTO attribute) throws IOException {
        attributes.add(attribute);
        SpringEntitiesAttributes.generateOrUpdateSpringEntityClass(projectName, entityName, attribute);
    }

    public List<AttributeDTO> getAttributes() {
        return new ArrayList<>(attributes); // Return a copy to avoid external modification
    }

    public void clearAttributes() {
        attributes.clear();
    }
}