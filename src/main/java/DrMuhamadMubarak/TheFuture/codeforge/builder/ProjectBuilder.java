package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;

public class ProjectBuilder {
    public static Project projectBuilder(String projectName, String projectDescription, FrontendType projectFrontendType, BackendType projectBackendType, DatabaseType projectDatabaseType) {
        return Project.builder().name(projectName)
                .description(projectDescription)
                .frontendType(projectFrontendType)
                .backendType(projectBackendType)
                .databaseType(projectDatabaseType)
                .build();
    }
}
