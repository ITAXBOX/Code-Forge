package DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing endpoint information for the frontend generator
 */
@Getter
public class EndpointInfo {
    private final String method;
    private final String path;
    private final boolean isCustomBehavior;
    @Setter
    private List<ParameterInfo> parameters;
    @Setter
    private String description;

    public EndpointInfo(String method, String path, boolean isCustomBehavior) {
        this.method = method;
        this.path = path;
        this.isCustomBehavior = isCustomBehavior;
        this.parameters = new ArrayList<>();
    }

    public void addParameter(String name, String type, boolean required) {
        parameters.add(new ParameterInfo(name, type, required, "", ""));
    }

    public void addParameter(ParameterInfo parameterInfo) {
        parameters.add(parameterInfo);
    }

    public record ParameterInfo(String name, String type, boolean required, String description, String javaType) {

    }
}
