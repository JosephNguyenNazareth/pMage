package com.pmsconnect.mage.project;

import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.connector.ConnectorRepository;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ConnectorRepository connectorRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ConnectorRepository connectorRepository) {
        this.projectRepository = projectRepository;
        this.connectorRepository = connectorRepository;
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
        Map<String, String> connectorMap = new HashMap<>();

        for (String connectorId : connectorList) {
            Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));
            connector.setLinkedProjectId(projectId);
            connectorRepository.save(connector);

            connectorMap.put(connectorId, connector.getBridge().getProcessDef());
        }

        project.setParticipateConnectionIds(connectorMap);
        projectRepository.save(project);

    }

    public void addConnectors(Project project, List<Connector> connectorList) {
        Map<String, String> connectorMap = new HashMap<>();

        for (Connector connector : connectorList) {
            connector.setLinkedProjectId(project.getProjectId());
            connectorRepository.save(connector);
            connectorMap.put(connector.getId(), connector.getBridge().getProcessDef());
        }

        project.setParticipateConnectionIds(connectorMap);
        projectRepository.save(project);

    }

    public void addUsers(String projectId, String userIds) {
        Project project = this.getProject(projectId);
        List<String> userList = Arrays.asList(userIds.split(","));
        project.setParticipatingUserIds(userList);
        projectRepository.save(project);
    }

    public void addPair(String projectId, String predecessorProcess, String successorProcess, String prePoint, String prePointState, String sucPoint, String sucPointState) {
        Project project = this.getProject(projectId);
        CoordinationPair pair = new CoordinationPair(predecessorProcess, successorProcess, prePoint, prePointState, sucPoint, sucPointState);
        project.addCoordinationPoint(pair);
        projectRepository.save(project);
    }
}
