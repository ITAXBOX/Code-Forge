package DrMuhamadMubarak.TheFuture.codeforge.controller;

import DrMuhamadMubarak.TheFuture.codeforge.builder.ProjectCreateRequestBuilder;
import DrMuhamadMubarak.TheFuture.codeforge.dto.ProjectCreateDTO;
import DrMuhamadMubarak.TheFuture.codeforge.dto.ProjectUpdateDTO;
import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import DrMuhamadMubarak.TheFuture.codeforge.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public void getAllProjects(Model model) {
        List<Project> projects = projectService.getAllProjects();
        model.addAttribute("projects", projects);
    }

    @GetMapping("/{id}")
    public void getProjectDetails(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
    }

    @GetMapping("/new")
    public void showCreateForm(Model model) {
        model.addAttribute("project", new ProjectCreateDTO());
    }

    @PostMapping
    public void createProject(@ModelAttribute ProjectCreateDTO request) {
        projectService.createProject(request);
    }

    @GetMapping("/{id}/edit")
    public void showEditForm(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        ProjectCreateDTO dto = ProjectCreateRequestBuilder.projectCreateRequestDTOBuilder(
                project.getName(),
                project.getDescription(),
                project.getFrontendType().toString(),
                project.getBackendType().toString(),
                project.getDatabaseType().toString()
        );
        model.addAttribute("project", dto);
    }

    @PostMapping("/{id}")
    public void updateProject(@PathVariable Long id, @ModelAttribute ProjectUpdateDTO request) {
        projectService.updateProject(id, request);
    }

    @GetMapping("/{id}/delete")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @GetMapping("/count")
    @ResponseBody
    public long countProjects() {
        return projectService.countProjects();
    }
}
