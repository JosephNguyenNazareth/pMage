package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.config.PmsConfig;
import com.pmsconnect.mage.config.AppConfig;
import com.pmsconnect.mage.user.Bridge;
import com.pmsconnect.mage.utils.ActionEvent;
import com.pmsconnect.mage.utils.Alignment;
import com.pmsconnect.mage.utils.Artifact;
import com.pmsconnect.mage.utils.TaskArtifact;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Document(collection = "connector_test")
public class Connector {
    @Id
    private String id;
    private Bridge bridge;
    private boolean isArtifactCentric;
    private Map<String, List<ActionEvent>> actionLinkage;
    private String actionEventDescription;
    private List<Alignment> historyTriggerList;
    private Map<String, String> monitoringLog;
    private boolean monitoring;
    private AppConfig appConfig;
    private PmsConfig pmsConfig;
    private String userName;
    private Map<String, Artifact> artifactPool;
    private List<TaskArtifact> taskArtifactList;
    private List<String> publicTasks;
    private String linkedProjectId;

    public Connector() {
        this.loadProperties();
    }

    public void loadProperties() {
        try {
            InputStream file = Connector.class.getResourceAsStream("/application.properties");
            if (file!=null)
                System.getProperties().load(file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public Connector(Bridge bridge) {
        this.id = UUID.randomUUID().toString();
        this.historyTriggerList = new ArrayList<>();
        this.monitoringLog = new HashMap<>();
        this.monitoring = false;
        this.actionLinkage = new HashMap<>();
        this.bridge = bridge;
        this.loadProperties();
        this.appConfig = new AppConfig(System.getProperty("appconfig"), this.bridge.getAppName());
        this.pmsConfig = new PmsConfig(System.getProperty("pmsconfig"), this.bridge.getPmsName());
        this.isArtifactCentric = this.pmsConfig.isArtifactCentric();
        if (!this.isArtifactCentric)
            this.actionLinkage.put("ALL", new ArrayList<>());
        this.artifactPool = new HashMap<>();
    }

    public Connector(String userName, Bridge bridge) {
        this.id = UUID.randomUUID().toString();
        this.historyTriggerList = new ArrayList<>();
        this.monitoringLog = new HashMap<>();
        this.monitoring = false;
        this.actionLinkage = new HashMap<>();
        this.bridge = bridge;
        this.userName = userName;
        this.loadProperties();
        this.appConfig = new AppConfig(System.getProperty("appconfig"), this.bridge.getAppName());
        this.pmsConfig = new PmsConfig(System.getProperty("pmsconfig"), this.bridge.getPmsName());
        this.isArtifactCentric = this.pmsConfig.isArtifactCentric();
        if (!this.isArtifactCentric)
            this.actionLinkage.put("all", new ArrayList<>());
        this.artifactPool = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public List<Alignment> getHistoryTriggerList() {
        return historyTriggerList;
    }

    public void setHistoryTriggerList(List<Alignment> historyTriggerList) {
        this.historyTriggerList = historyTriggerList;
    }

    public void addHistoryTriggerList(String historyCommit, String processInstanceChange, String commitTime, String changeTime, Boolean isViolated, String taskFound, String monitoringMessage) {
        this.historyTriggerList.add(new Alignment(historyCommit, processInstanceChange, commitTime, changeTime, isViolated, taskFound, monitoringMessage));
    }

    public void addHistoryTriggerList(String historyCommit, String commitTime, Boolean isViolated) {
        this.historyTriggerList.add(new Alignment(historyCommit, "", commitTime, "", isViolated, "", ""));
    }

    public void addHistoryTriggerList(Alignment alignment) {
        this.historyTriggerList.add(alignment);
    }

    public Alignment findTriggeredActionId(String actionId) {
        for (Alignment alignment: this.historyTriggerList) {
            if (alignment.getTriggeredActionId().equals(actionId))
                return alignment;
        }
        return null;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public PmsConfig getPmsConfig() {
        return pmsConfig;
    }

    public void setPmsConfig(PmsConfig pmsConfig) {
        this.pmsConfig = pmsConfig;
    }

    public Map<String, String> getMonitoringLog() {
        return monitoringLog;
    }

    public void addMonitoringLog(String monitoringMess) {
        this.monitoringLog.put(LocalDateTime.now().toString(), monitoringMess);
    }

    public void setMonitoringLog(Map<String, String> monitoringLog) {
        this.monitoringLog = monitoringLog;
    }

    public Bridge getBridge() {
        return bridge;
    }

    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }

    public Map<String, List<ActionEvent>> getActionLinkage() {
        return actionLinkage;
    }

    public void addActionEvent(String artifact, ActionEvent actionEvent) {
        this.actionLinkage.computeIfAbsent(artifact, k -> new ArrayList<>()).add(actionEvent);
    }

    public void addActionEvent(String artifact, List<ActionEvent> actionEvent) {
        this.actionLinkage.put(artifact, actionEvent);
    }

    public void setActionLinkage(Map<String, List<ActionEvent>> actionLinkage) {
        this.actionLinkage = actionLinkage;
    }

    public String getActionEventDescription() {
        return actionEventDescription;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setActionEventDescription(String actionEventDescription) {
        this.actionEventDescription = actionEventDescription;
    }

    public String getLinkedProjectId() {
        return linkedProjectId;
    }

    public void setLinkedProjectId(String linkedProjectId) {
        this.linkedProjectId = linkedProjectId;
    }

    public Map<String, Artifact> getArtifactPool() {
        return artifactPool;
    }

    public void setArtifactPool(Map<String, Artifact> artifactPool) {
        this.artifactPool = artifactPool;
    }

    public void addArtifact(Artifact artifact) {
        this.artifactPool.put(artifact.getName(), artifact);
    }

    public void addArtifact(String name) {
        this.artifactPool.put(name, new Artifact(name));
    }

    public List<String> getPublicTasks() {
        return publicTasks;
    }

    public void setPublicTasks(List<String> publicTasks) {
        this.publicTasks = publicTasks;
    }

    public void addPublicTask(String publicTask) {
        if (!this.publicTasks.contains(publicTask))
            this.publicTasks.add(publicTask);
    }

    public boolean existActionEvent(String artifact, String appAction, String pmsAction) {
        for (ActionEvent actionEvent : this.actionLinkage.get(artifact)) {
            if (actionEvent.getPmsAction().equals(pmsAction) && actionEvent.getAppAction().equals(appAction))
                return true;
        }
        return false;
    }

    public void updateConfig() {
        this.pmsConfig.readConfig();
        this.appConfig.readConfig();
    }

    public List<TaskArtifact> getTaskArtifactList() {
        return taskArtifactList;
    }

    public void setTaskArtifactList(List<TaskArtifact> taskArtifactList) {
        this.taskArtifactList = taskArtifactList;
    }

    public void updateTaskArtifactList(List<String> taskArtifactStringList) {
        for (String taskArtifactString : taskArtifactStringList) {
            String[] taskArtifactStringParse = taskArtifactString.split(" : ");
            String[] input = taskArtifactStringParse[0].split(",");
            String taskName = taskArtifactStringParse[1];
            String[] output = taskArtifactStringParse[2].split(",");

            this.taskArtifactList.add(new TaskArtifact(taskName, input, output, "string"));
        }
    }
}
