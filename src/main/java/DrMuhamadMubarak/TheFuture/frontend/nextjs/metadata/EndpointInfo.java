package DrMuhamadMubarak.TheFuture.frontend.nextjs.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Model class representing endpoint information for the frontend generator
 */
@Getter
@AllArgsConstructor
public class EndpointInfo {
    private final String method;
    private final String path;
    private final boolean isCustomBehavior;
}
