package DrMuhamadMubarak.TheFuture.utils;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EntityContext {
    private final Map<String, List<AttributeDTO>> entityAttributes = new HashMap<>();

    public void addEntity(String entityName, List<AttributeDTO> attributes) {
        entityAttributes.put(entityName, attributes);
    }

    public List<AttributeDTO> getAttributesFor(String entityName) {
        return entityAttributes.getOrDefault(entityName, List.of());
    }

    public boolean hasEntity(String entityName) {
        return entityAttributes.containsKey(entityName);
    }
}