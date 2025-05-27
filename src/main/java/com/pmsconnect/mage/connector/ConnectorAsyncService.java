package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.project.Project;
import com.pmsconnect.mage.project.ProjectRepository;
import com.pmsconnect.mage.project.coordination.ActivityState;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import com.pmsconnect.mage.utils.ActionEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

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

        if (!connector.isMonitoring())
            connector.setMonitoring(true);
        connectorRepository.save(connector);
        this.watchProject(connector);
//        else {
//            throw new IllegalStateException("This connector is already monitored");
//        }
    }

    @Async
    public void watchProject(Connector connector) {
        StringBuilder monitoringMessAll = new StringBuilder();
        monitoringMessAll.append("Fresh monitoring connector " + connector.getId() + " of project id" + connector.getBridge().getProcessInstanceId() + "\n");
        connector.addMonitoringLog(monitoringMessAll.toString());
        System.out.println("hello " + connector.getId());

        // then run this as a background service to watch user activities
        while(true) {
            try {
                // check if user stops pmage watching
                Connector updatedConnector = connectorRepository.findById(connector.getId()).orElseThrow(() -> new IllegalStateException("Connector with id " + connector.getId() + "does not exist."));
                if (!updatedConnector.isMonitoring())
                    return;

                StringBuilder monitoringMess = new StringBuilder();
                monitoringMess.append("Monitoring connector " + connector.getId() + " of project id" + connector.getBridge().getProcessInstanceId() + " of user " + connector.getBridge().getProcessInstanceId() + "\n");

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

                                Project linkedProject = projectRepository.findById(connector.getLinkedProjectId())
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Connector with id " + connector.getLinkedProjectId() + " does not exist."));

                                // we need to check with the coordination pair as well, before calling PMS action
                                boolean validated = linkedProject.getCoordinationPoints().stream()
                                        .filter(pair -> pair.getSuccessorPoint().equals(actionEvent.getTask()))
                                        .allMatch(pair -> {
                                            boolean ok = pair.getPrePointDesiredState().equals(pair.getPrePointActualState());
                                            // if the current task is the successor of a coordination pair
                                            // verify if its corresponding predecessor's actual state meets the desired state
                                            // if not, alarm user about the issue
                                            if (!ok) {
                                                String error = String.format("Task %s is waiting for %s to be in state %s. Current state %s.",
                                                        actionEvent.getTask(),
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
                                Map<String, String> inputValues = connector.getBridge().toMap();
                                inputValues.putAll(actionEvent.toMap());
                                connector.getPmsConfig().callApiWithDependencies(actionEvent.getPmsEvent(), inputValues);
                                actionEvent.setStatus("done");
                                connectorRepository.save(connector);

                                // Update coordination pair states
                                for (CoordinationPair pair : linkedProject.getCoordinationPoints()) {
                                    String task = actionEvent.getTask();
                                    String event = actionEvent.getPmsEvent();

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


                connector.addMonitoringLog(monitoringMess.toString());
                // check user activities every 5 seconds
                Thread.sleep(5000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void cleanup(Connector connector) {
        connector.setMonitoring(false);
        connectorRepository.save(connector);
    }
}
