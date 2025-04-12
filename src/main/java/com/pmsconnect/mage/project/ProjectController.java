package com.pmsconnect.mage.project;

import com.pmsconnect.mage.user.Bridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/pmage/project")
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> getProjects() {
        return projectService.getProjects();
    }

    @PostMapping(path = "/add")
    public String addNewProject(
            @RequestParam(required = true) String projectManager) {
        return projectService.addNewProject(projectManager);
    }

    @PostMapping(path = "/add-connectors")
    public void addConnectors(
            @PathVariable("projectId") String projectId,
            @RequestParam(required = true) String connectorIds) {
        projectService.addConnectors(projectId, connectorIds);
    }

    @PostMapping(path = "/add-users")
    public void addUsers(
            @PathVariable("projectId") String projectId,
            @RequestParam(required = true) String userIds) {
        projectService.addUsers(projectId, userIds);
    }

    @PostMapping(path = "/add-pair")
    public void addPair(
            @PathVariable("projectId") String projectId,
            @RequestParam(required = true) String predecessorProcess,
            @RequestParam(required = true) String successorProcess,
            @RequestParam(required = true) String preA,
            @RequestParam(required = false) String preAState,
            @RequestParam(required = true) String sucA,
            @RequestParam(required = false) String sucAState) {
        projectService.addPair(projectId, predecessorProcess, successorProcess, preA, preAState, sucA, sucAState);
    }
}
