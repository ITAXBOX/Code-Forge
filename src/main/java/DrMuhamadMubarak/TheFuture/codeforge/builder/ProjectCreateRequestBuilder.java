package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectCreateRequestDTO;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;

public class ProjectCreateRequestBuilder {
    public static ProjectCreateRequestDTO projectCreateRequestDTOBuilder(String name, String description, String frontendType, String backendType, String databaseType) {
        return ProjectCreateRequestDTO.builder()
                .name(name)
                .description(description)
                .frontendType(FrontendType.valueOf(frontendType))
                .backendType(BackendType.valueOf(backendType))
                .databaseType(DatabaseType.valueOf(databaseType))
                .build();
    }
}
