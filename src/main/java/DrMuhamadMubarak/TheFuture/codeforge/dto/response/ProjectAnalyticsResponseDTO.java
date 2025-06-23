package DrMuhamadMubarak.TheFuture.codeforge.dto.response;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectAnalyticsResponseDTO {
    private Long projectId;
    private String projectName;
    private String projectDescription;
    private FrontendType frontendType;
    private BackendType backendType;
    private DatabaseType databaseType;
    private int requestCount;
    private LocalDateTime lastRequestedAt;
}
