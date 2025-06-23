package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectAnalyticsResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;

public class ProjectAnalyticsResponseDTOBuilder {
    public static ProjectAnalyticsResponseDTO projectAnalyticsResponseDTObuilder(ProjectAnalytics projectAnalytics)
    {
        return ProjectAnalyticsResponseDTO.builder().
                requestCount(projectAnalytics.getRequestCount()).
                lastRequestedAt(projectAnalytics.getLastRequestedAt()).
                build();
    }
}
