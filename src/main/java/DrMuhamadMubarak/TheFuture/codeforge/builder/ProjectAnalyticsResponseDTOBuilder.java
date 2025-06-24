package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectAnalyticsResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;

public class ProjectAnalyticsResponseDTOBuilder {
    public static ProjectAnalyticsResponseDTO projectAnalyticsResponseDTObuilder(ProjectAnalytics projectAnalytics) {
        // If project analytics has no associated project (shouldn't happen normally)
        if (projectAnalytics.getProject() == null) {
            return ProjectAnalyticsResponseDTO.builder()
                    .requestCount(projectAnalytics.getRequestCount())
                    .lastRequestedAt(projectAnalytics.getLastRequestedAt())
                    .build();
        }
        // Include both analytics and project data in the response
        return ProjectAnalyticsResponseDTO.builder()
                .projectId(projectAnalytics.getProject().getId())
                .projectName(projectAnalytics.getProject().getName())
                .projectDescription(projectAnalytics.getProject().getDescription())
                .frontendType(projectAnalytics.getProject().getFrontendType())
                .backendType(projectAnalytics.getProject().getBackendType())
                .databaseType(projectAnalytics.getProject().getDatabaseType())
                .requestCount(projectAnalytics.getRequestCount())
                .createdAt(projectAnalytics.getProject().getCreatedAt())
                .lastRequestedAt(projectAnalytics.getLastRequestedAt())
                .build();
    }
}
