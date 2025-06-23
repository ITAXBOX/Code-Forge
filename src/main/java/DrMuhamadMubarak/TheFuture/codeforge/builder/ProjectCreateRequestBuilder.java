package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.dto.ProjectCreateDTO;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;

public class ProjectCreateRequestBuilder {
    public static ProjectCreateDTO projectCreateRequestDTOBuilder(String name, String description, FrontendType frontendType, BackendType backendType, DatabaseType databaseType) {
        return ProjectCreateDTO.builder()
                .name(name)
                .description(description)
                .frontendType(frontendType)
                .backendType(backendType)
                .databaseType(databaseType)
                .build();
    }
}
