package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.casestudy.PreDefinedArtifactInstance;
import com.pmsconnect.mage.config.AppConfig;
import com.pmsconnect.mage.kie.KieServer;
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

    @Autowired
    public ConnectorAsyncService (ConnectorRepository mageRepository) {
        this.connectorRepository = mageRepository;
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
                    List<ActionEvent> artifactActionLinkage = actionLinkage.get(artifact);

                    Map<String, List<String>> appEventChecklist = new HashMap<>();
                    for (ActionEvent actionEvent : artifactActionLinkage) {
                        appEventChecklist.computeIfAbsent(actionEvent.getAppEvent(), k -> new ArrayList<>()).add(actionEvent.getContextInfo());
                    }

                    for (String appEvent : appEventChecklist.keySet()) {
                        this.callAppEvent(connector.getAppConfig(), appEvent, appEventChecklist.get(appEvent));
                    }
                }

                // TODO: checking pms log to detect manual pms updates
                this.checkingPMSLog(connector);

                // TODO: if the task marked done by the PMS triggered a coordination pair
                // TODO: to emit another event to the PMS of the following task

                connector.addMonitoringLog(monitoringMess.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void callAppEvent(AppConfig appConfig, String appEvent, List<String> contextInfoList) {
        // if the context info is found from appEvent, call corresponding API to pms
        appConfig.buildAPILink("", appConfig.getConfig(), appEvent);
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
