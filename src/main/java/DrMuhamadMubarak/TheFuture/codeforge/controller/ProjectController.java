package DrMuhamadMubarak.TheFuture.codeforge.controller;

import DrMuhamadMubarak.TheFuture.codeforge.builder.ProjectRequestBuilder;
import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectCreateDTO;
import DrMuhamadMubarak.TheFuture.codeforge.dto.request.ProjectUpdateDTO;
import DrMuhamadMubarak.TheFuture.codeforge.dto.response.ProjectResponseDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        List<ProjectResponseDTO> projectResponseDTOS = projects.stream()
                .map(ProjectRequestBuilder::toResponseDTO)
                .toList();
        return ResponseEntity.ok(projectResponseDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectDetails(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        ProjectResponseDTO projectResponseDTO = ProjectRequestBuilder.toResponseDTO(project);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectCreateDTO request) {
        Project createdProject = projectService.createProject(request);
        ProjectResponseDTO createdProjectResponseDTO = ProjectRequestBuilder.toResponseDTO(createdProject);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProjectResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable Long id, @RequestBody ProjectUpdateDTO request) {
        Project updatedProject = projectService.updateProject(id, request);
        ProjectResponseDTO updatedProjectResponseDTO = ProjectRequestBuilder.toResponseDTO(updatedProject);
        return ResponseEntity.ok(updatedProjectResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countProjects() {
        long count = projectService.countProjects();
        return ResponseEntity.ok(count);
    }
}
