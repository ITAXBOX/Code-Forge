package DrMuhamadMubarak.TheFuture.codeforge.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProjectUpdateDTO {
    @Size(min = 3, max = 50)
    private String name;

    @Size(max = 500)
    private String description;
}
