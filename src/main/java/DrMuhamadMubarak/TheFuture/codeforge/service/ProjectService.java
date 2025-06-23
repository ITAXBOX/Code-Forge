package DrMuhamadMubarak.TheFuture.codeforge.service;

import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectCreateRequestDTO;
import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectUpdateRequestDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.codeforge.model.ProjectAnalytics;
import DrMuhamadMubarak.TheFuture.codeforge.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public void createProject(ProjectCreateRequestDTO request) {
        if (projectRepository.existsByName((request.getName()))) {
            throw new IllegalArgumentException("Project name already exists");
        }
        Project project = new Project(
                request.getName(),
                request.getDescription(),
                request.getFrontendType(),
                request.getBackendType(),
                request.getDatabaseType()
        );
        ProjectAnalytics analytics = new ProjectAnalytics();
        project.setAnalytics(analytics);
        analytics.setProject(project);
        projectRepository.save(project);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    public List<Project> getAllProjects() {
        return (List<Project>) projectRepository.findAll();
    }

    @Transactional
    public Project updateProject(Long id, ProjectUpdateRequestDTO request) {
        Project project = getProjectById(id);
        if (request.getName() != null && !request.getName().equals(project.getName())) {
            if (projectRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Project name already exists");
            }
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }
}