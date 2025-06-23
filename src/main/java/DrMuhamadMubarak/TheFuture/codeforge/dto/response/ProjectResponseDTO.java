package DrMuhamadMubarak.TheFuture.codeforge.dto.response;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDTO {
    private String name;
    private String description;
    private FrontendType frontendType;
    private BackendType backendType;
    private DatabaseType databaseType;
    private ProjectAnalyticsResponseDTO projectAnalyticsDTO; // Optional, can be null if no analytics available
}