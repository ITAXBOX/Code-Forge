package DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata;

import lombok.Getter;

/**
 * Model class representing entity attribute information for the frontend generator
 */
@Getter
public class EntityAttributeInfo {
    private final String name;
    private final String type;

    public EntityAttributeInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

