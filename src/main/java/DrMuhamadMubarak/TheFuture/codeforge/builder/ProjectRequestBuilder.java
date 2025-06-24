package DrMuhamadMubarak.TheFuture.codeforge.builder;

import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectAnalyticsResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;

public class ProjectRequestBuilder {
    /**
     * Creates a ProjectResponseDTO from a Project entity, including analytics information if available.
     * This avoids infinite recursion by using DTOs instead of entities.
     */
    public static ProjectResponseDTO toResponseDTO(Project project) {
        ProjectResponseDTO.ProjectResponseDTOBuilder builder = ProjectResponseDTO.builder()
                .name(project.getName())
                .description(project.getDescription())
                .frontendType(project.getFrontendType())
                .backendType(project.getBackendType())
                .databaseType(project.getDatabaseType())
                .createdAt(project.getCreatedAt());

        // Add analytics if available
        if (project.getAnalytics() != null) {
            ProjectAnalytics analytics = project.getAnalytics();
            ProjectAnalyticsResponseDTO analyticsDTO = ProjectAnalyticsResponseDTO.builder()
                    .requestCount(analytics.getRequestCount())
                    .lastRequestedAt(analytics.getLastRequestedAt())
                    .build();
            builder.projectAnalyticsDTO(analyticsDTO);
        }

        return builder.build();
    }
}
