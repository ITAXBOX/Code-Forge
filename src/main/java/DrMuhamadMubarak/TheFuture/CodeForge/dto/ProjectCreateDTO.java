package DrMuhamadMubarak.TheFuture.codeforge.dto;

import DrMuhamadMubarak.TheFuture.generator.enums.BackendType;
import DrMuhamadMubarak.TheFuture.generator.enums.DatabaseType;
import DrMuhamadMubarak.TheFuture.generator.enums.FrontendType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateDTO {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private FrontendType frontendType;

    @NotNull
    private BackendType backendType;

    @NotNull
    private DatabaseType databaseType;
}
