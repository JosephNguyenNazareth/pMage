package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.config.PmsConfig;
import com.pmsconnect.mage.user.UserPMage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConnectorService {
    private final ConnectorRepository connectorRepository;

    @Autowired
    public ConnectorService(ConnectorRepository mageRepository) {
        this.connectorRepository = mageRepository;
    }

    public List<Connector> getConnectors() {
        return connectorRepository.findAll();
    }

    public Connector getConnector(String connectorId) {

        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        return connector;
    }

    public String addNewConnector(UserPMage user) {
        if (!verifyPmsExist(user))
            throw new IllegalStateException("Cannot verify pms");
        Connector connector = new Connector(user);

        connectorRepository.save(connector);

        return connector.getId();
    }

    @Transactional
    public void updateConnector(String connectorId, UserPMage user) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        if (user != null && !user.equals(connector.getUserPMage())) {
            if (!verifyPmsExist(user))
                throw new IllegalStateException("Cannot verify pms");
            connector.setUserPMage(user);
        }

        connectorRepository.save(connector);
    }

    private boolean verifyPmsExist(UserPMage user) {
        HttpClient client = HttpClients.createDefault();
        try {
            PmsConfig tmpConfig = new PmsConfig("./src/main/resources/pms_config.json", user.getPmsName());

            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", tmpConfig.getUrlPMS());
            urlMap.put("projectId", user.getProjectId());

            String finalUri = tmpConfig.buildAPI("verify", urlMap, paramMap);
            HttpGet getMethod = new HttpGet(finalUri);
            HttpResponse getResponse = client.execute(getMethod);

            int getStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            return getStatusCode == 200;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProcessInstance(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + " does not exist."));

        HttpClient client = HttpClients.createDefault();
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrlPMS());
            urlMap.put("projectId", connector.getUserPMage().getProjectId());

            String finalUri = connector.getPmsConfig().buildAPI("verify", urlMap, paramMap);
            HttpGet getMethod = new HttpGet(finalUri);
            HttpResponse getResponse = client.execute(getMethod);

            int getStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            if (getStatusCode == 200)
                return EntityUtils.toString(getResponse.getEntity());
            return "";
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String createProcessInstance(String connectorId, String processName) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        if (!createPMSProcessInstance(connector, processName, connector.getUserPMage().getRealName()))
            throw new IllegalStateException("Cannot create new process instance");
        connectorRepository.save(connector);

        return connector.getUserPMage().getProjectId();
    }

    private boolean createPMSProcessInstance(Connector connector, String processName, String creatorName) {
        HttpClient client = HttpClients.createDefault();
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrlPMS());
            paramMap.put("processName", processName);
            paramMap.put("creatorName", creatorName);

            String finalUri = connector.getPmsConfig().buildAPI("createProject", urlMap, paramMap);
            HttpPost postMethod = new HttpPost(finalUri);
            HttpResponse getResponse = client.execute(postMethod);

            int postStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            if (postStatusCode != 200)
                return false;
            else {
                String responseBody = EntityUtils.toString(getResponse.getEntity());
                connector.getUserPMage().setProjectId(responseBody);
                return true;
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopMonitoringProcessInstance(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        connector.setMonitoring(false);
        connectorRepository.save(connector);
        this.closeProcess(connector);
        System.out.println("Stop monitoring connector with id " + connectorId);
    }

    private void closeProcess(Connector connector) {
        HttpClient client = HttpClients.createDefault();
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrlPMS());
            urlMap.put("projectId", connector.getUserPMage().getProjectId());
            paramMap.put("processInstanceState", "true");

            String finalUri = connector.getPmsConfig().buildAPI("changeProjectState", urlMap, paramMap);
            HttpPut putMethod = new HttpPut(finalUri);
            HttpResponse getResponse = client.execute(putMethod);

            int getStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            if (getStatusCode != 200)
                throw new IllegalStateException("Cannot close process instance id " + connector.getUserPMage().getProjectId());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteConnector(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        connectorRepository.delete(connector);
    }
}
