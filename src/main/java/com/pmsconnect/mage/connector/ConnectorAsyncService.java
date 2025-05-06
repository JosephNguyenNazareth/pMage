package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.casestudy.PreDefinedArtifactInstance;
import com.pmsconnect.mage.config.AppConfig;
import com.pmsconnect.mage.kie.KieServer;
import com.pmsconnect.mage.project.Project;
import com.pmsconnect.mage.project.ProjectRepository;
import com.pmsconnect.mage.project.coordination.ActivityState;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import com.pmsconnect.mage.utils.ActionEvent;
import com.pmsconnect.mage.utils.Alignment;

import com.pmsconnect.mage.utils.Artifact;
import com.pmsconnect.mage.utils.Transition;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectorAsyncService {
    private final ConnectorRepository connectorRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public ConnectorAsyncService (ConnectorRepository mageRepository, ProjectRepository projectRepository) {
        this.connectorRepository = mageRepository;
        this.projectRepository = projectRepository;
    }

    @Async
    public void monitorProcessInstance(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        if (!connector.isMonitoring()) {
            connector.setMonitoring(true);
            connectorRepository.save(connector);
            this.watchProject(connector);
        } else {
            throw new IllegalStateException("This connector is already monitored");
        }
    }


    public void watchProject(Connector connector) {
        StringBuilder monitoringMessAll = new StringBuilder();
        monitoringMessAll.append("Fresh monitoring connector " + connector.getId() + " of project id" + connector.getBridge().getProcessId() + "\n");
        connector.addMonitoringLog(monitoringMessAll.toString());

        // then run this as a background service to watch user activities
        while(true) {
            try {
                // check user activities every 5 seconds
                Thread.sleep(5000);

                // check if user stops pmage watching
                Connector updatedConnector = connectorRepository.findById(connector.getId()).orElseThrow(() -> new IllegalStateException("Connector with id " + connector.getId() + "does not exist."));
                if (!updatedConnector.isMonitoring())
                    return;

                StringBuilder monitoringMess = new StringBuilder();
                monitoringMess.append("Monitoring connector " + connector.getId() + " of project id" + connector.getBridge().getProcessId() + " of user " + connector.getBridge().getProcessId() + "\n");

                // call all app actions defined in the action linkage
                // however, we should know which task is currently available to be done ???
                Map<String, List<ActionEvent>> actionLinkage = connector.getActionLinkage();
                for (String artifact : actionLinkage.keySet()) {
                    List<ActionEvent> allArtifactActionLinkage = actionLinkage.get(artifact);

                    // select only untriggered app event to check
                    List<ActionEvent> artifactActionLinkage = new ArrayList<>();
                    for (ActionEvent actionEvent: allArtifactActionLinkage) {
                        if (actionEvent.getStatus().equals("ready"))
                            artifactActionLinkage.add(actionEvent);
                    }

                    Map<String, List<String>> appEventChecklist = new HashMap<>();
                    for (ActionEvent actionEvent : artifactActionLinkage) {
                        appEventChecklist.computeIfAbsent(actionEvent.getAppEvent(), k -> new ArrayList<>()).add(actionEvent.getContextInfo());
                    }

                    // notation : the list of app event check list contains only untriggered events
                    for (String appEvent : appEventChecklist.keySet()) {
                        Map<String, String> appEventTriggered = connector.getAppConfig().emitAppEvent(connector, appEvent, appEventChecklist.get(appEvent));
                        // if there is new triggered app event
                        if (appEventTriggered != null) {
                            for (ActionEvent actionEvent : artifactActionLinkage) {
                                boolean isMatchingEvent = actionEvent.getAppEvent().equals(appEvent) &&
                                        actionEvent.getContextInfo().equals(appEventTriggered.get("task"));
                                if (!isMatchingEvent) continue;

                                ActionEvent actionEventTriggered = actionEvent;

                                Project linkedProject = projectRepository.findById(connector.getLinkedProjectId())
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Connector with id " + connector.getLinkedProjectId() + " does not exist."));

                                // we need to check with the coordination pair as well, before calling PMS action
                                boolean validated = linkedProject.getCoordinationPoints().stream()
                                        .filter(pair -> pair.getSuccessorPoint().equals(actionEventTriggered.getTask()))
                                        .allMatch(pair -> {
                                            boolean ok = pair.getPrePointDesiredState().equals(pair.getPrePointActualState());
                                            // if the current task is the successor of a coordination pair
                                            // verify if its corresponding predecessor's actual state meets the desired state
                                            // if not, alarm user about the issue
                                            if (!ok) {
                                                String error = String.format("Task %s is waiting for %s to be in state %s. Current state %s.",
                                                        actionEventTriggered.getTask(),
                                                        pair.getPredecessorPoint(),
                                                        pair.getPrePointDesiredState(),
                                                        pair.getPrePointActualState());
                                                System.out.println(error);
                                                connector.addMonitoringLog(error);
                                            }
                                            return ok;
                                        });

                                if (!validated) break;

                                // Call PMS API
                                connector.getPmsConfig().callApiWithDependencies(
                                        actionEvent.getPmsEvent(), connector.getBridge().toMap());

                                // Update coordination pair states
                                for (CoordinationPair pair : linkedProject.getCoordinationPoints()) {
                                    String task = actionEventTriggered.getTask();
                                    String event = actionEventTriggered.getPmsEvent();

                                    if (pair.getSuccessorPoint().equals(task)) {
                                        if (event.equals("startTask"))
                                            pair.setSucPointActualState(ActivityState.STARTED);
                                        else if (event.equals("finishTask"))
                                            pair.setSucPointActualState(ActivityState.FINISHED);
                                    } else if (pair.getPredecessorPoint().equals(task)) {
                                        if (event.equals("startTask"))
                                            pair.setPrePointActualState(ActivityState.STARTED);
                                        else if (event.equals("finishTask"))
                                            pair.setPrePointActualState(ActivityState.FINISHED);
                                    }
                                }

                                projectRepository.save(linkedProject);
                                break;
                            }
                            break;
                        }
                    }
                }

                // TODO: checking pms log to detect manual pms updates
                this.checkingPMSLog(connector);

                    connector.addMonitoringLog(monitoringMess.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void checkingPMSLog(Connector connector) {
        // retrieve pms log
        HttpClient client = HttpClients.createDefault();
        String log = "";
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrl());

            String finalUri = connector.getPmsConfig().buildAPI("log", urlMap, paramMap);
            HttpGet getMethod = new HttpGet(finalUri);
            HttpResponse getResponse = client.execute(getMethod);

            int getStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            if (getStatusCode == 200) {
                String content = EntityUtils.toString(getResponse.getEntity());
                if (connector.getBridge().getPmsName().equals("core-bape"))
                    log = content;
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        // detect manual update
        // if not, ignore
        // if yes, retrieve the update (mostly complete the task)
        // check if the task related artifact is in the artifact pool

    }
}
