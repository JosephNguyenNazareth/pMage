package com.pmsconnect.mage.project;

import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import com.pmsconnect.mage.user.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "project")
public class Project {
    @Id
    private String projectId;
    private String projectManager;
    private List<String> participatingUserIds;
    private List<String> participateConnectionIds;
    private Map<String, CoordinationPair> coordinationPoints;

    public Project(String projectManager) {
        this.projectId = UUID.randomUUID().toString();
        this.projectManager = projectManager;
    }

    public Project(String projectManager, List<String> participatingUserIds) {
        this.projectId = UUID.randomUUID().toString();
        this.projectManager = projectManager;
        this.participatingUserIds = participatingUserIds;
    }

    public Project(String projectManager, List<String> participatingUserIds, List<String> participateConnectionIds) {
        this.projectId = UUID.randomUUID().toString();
        this.projectManager = projectManager;
        this.participatingUserIds = participatingUserIds;
        this.participateConnectionIds = participateConnectionIds;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManagers(String projectManager) {
        this.projectManager = projectManager;
    }

    public List<String> getParticipatingUserIds() {
        return participatingUserIds;
    }

    public void setParticipatingUserIds(List<String> participatingUserIds) {
        this.participatingUserIds = participatingUserIds;
    }

    public void addParticipatingUser(String userId) {
        this.participatingUserIds.add(userId);
    }

    public List<String> getParticipateConnectionIds() {
        return participateConnectionIds;
    }

    public void setParticipateConnectionIds(List<String> participateConnectionIds) {
        this.participateConnectionIds = participateConnectionIds;
    }

    public void addParticipatingConnectionId(String connectorId) {
        this.participateConnectionIds.add(connectorId);
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public Map<String, CoordinationPair> getCoordinationPoints() {
        return coordinationPoints;
    }

    public void setCoordinationPoints(Map<String, CoordinationPair> coordinationPoints) {
        this.coordinationPoints = coordinationPoints;
    }

    public void addCoordinationPoint(String predecessorPoint, CoordinationPair coordinationPair) {
        this.coordinationPoints.put(predecessorPoint, coordinationPair);
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId='" + projectId + '\'' +
                ", projectManager='" + projectManager + '\'' +
                ", participatingUserIds=" + participatingUserIds +
                ", participateConnectionIds=" + participateConnectionIds +
                ", coordinationPoints=" + coordinationPoints +
                '}';
    }
}
