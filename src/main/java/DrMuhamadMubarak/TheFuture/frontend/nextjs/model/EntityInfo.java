package DrMuhamadMubarak.TheFuture.frontend.nextjs.model;

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

    public EntityInfo(String name) {
        this.name = name;
        this.endpoints = new ArrayList<>();
        this.baseEndpoint = "/api/" + name.toLowerCase() + "s";
    }

    public void addEndpoint(String method, String path) {
        endpoints.add(new EndpointInfo(method, path, false));
    }

    public void addEndpoint(String method, String path, boolean isCustomBehavior) {
        endpoints.add(new EndpointInfo(method, path, isCustomBehavior));
    }

}
