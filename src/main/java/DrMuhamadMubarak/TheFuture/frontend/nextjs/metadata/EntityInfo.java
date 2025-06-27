package DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing entity information for the frontend generator
 */
@Getter
public class EntityInfo {
    private final String name;
    private final List<EndpointInfo> endpoints;
    @Setter
    private String baseEndpoint;

    // New field to store entity attributes
    private final List<EntityAttributeInfo> attributes;

    public EntityInfo(String name) {
        this.name = name;
        this.endpoints = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.baseEndpoint = "/api/" + name.toLowerCase() + "s";
    }

    public void addEndpoint(String method, String path) {
        endpoints.add(new EndpointInfo(method, path, false));
    }

    public void addEndpoint(String method, String path, boolean isCustomBehavior) {
        endpoints.add(new EndpointInfo(method, path, isCustomBehavior));
    }

    public void addBehaviorEndpoint(EndpointInfo endpointInfo) {
        endpoints.add(endpointInfo);
    }

    // New method to add an attribute
    public void addAttribute(String name, String type) {
        attributes.add(new EntityAttributeInfo(name, type));
    }
}
