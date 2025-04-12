package com.pmsconnect.mage.project;

import com.pmsconnect.mage.project.coordination.ActivityPair;
import com.pmsconnect.mage.project.coordination.ArtifactPair;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    public Project getProject(String projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new IllegalStateException("Project with id " + projectId + "does not exist."));
    }

    public String addNewProject(String projectManager) {
        Project newProject = new Project(projectManager);
        projectRepository.save(newProject);
        return newProject.getProjectId();
    }

    public void addConnectors(String projectId, String connectorIds) {
        Project project = this.getProject(projectId);
        List<String> connectorList = Arrays.asList(connectorIds.split(","));
        project.setParticipateConnectionIds(connectorList);
        projectRepository.save(project);
    }

    public void addUsers(String projectId, String userIds) {
        Project project = this.getProject(projectId);
        List<String> userList = Arrays.asList(userIds.split(","));
        project.setParticipatingUserIds(userList);
        projectRepository.save(project);
    }

    public void addPair(String projectId, String predecessorProcess, String successorProcess, String preA, String preAState, String sucA, String sucAState) {
        Project project = this.getProject(projectId);
        CoordinationPair pair;
        if (!preAState.isEmpty() && !sucAState.isEmpty()) {
            pair = new ArtifactPair(predecessorProcess, successorProcess, preA, preAState, sucA, sucAState);
        } else {
            pair = new ActivityPair(predecessorProcess, successorProcess, preA, sucA);
        }
        project.addCoordinationPoint(pair);
        projectRepository.save(project);
    }
}
