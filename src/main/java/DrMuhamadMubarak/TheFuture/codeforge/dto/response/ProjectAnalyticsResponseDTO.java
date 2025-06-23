package DrMuhamadMubarak.TheFuture.codeforge.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectAnalyticsResponseDTO {
    private int requestCount;
    private LocalDateTime lastRequestedAt;
}
