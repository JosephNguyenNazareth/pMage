package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.config.AppConfigManager;
import com.pmsconnect.mage.config.PMSConfigManager;
import com.pmsconnect.mage.config.PmsConfig;
import com.pmsconnect.mage.user.Bridge;
import com.pmsconnect.mage.user.User;
import com.pmsconnect.mage.user.UserRepository;
import com.pmsconnect.mage.utils.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.util.*;

@Service
public class ConnectorService {
    private final ConnectorRepository connectorRepository;
    private final SuppConnectorRepository suppConnectorRepository;
    private final UserRepository userRepository;

    @Autowired
    public ConnectorService(ConnectorRepository mageRepository, SuppConnectorRepository mageRepository2, UserRepository userRepository) {
        this.connectorRepository = mageRepository;
        this.suppConnectorRepository = mageRepository2;
        this.userRepository = userRepository;
        this.loadProperties();
    }

    public List<Connector> getConnectors() {
        return connectorRepository.findAll();
    }

    public Connector getConnector(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        return connector;
    }

    public List<Connector> getConnectorsByUserName(String userName) {
        List<Connector> allConnectors = connectorRepository.findAll();
        List<Connector> userConnectors = new ArrayList<>();
        for (Connector connector: allConnectors) {
            if (connector.getUserName().equals(userName)) {
                userConnectors.add(connector);

                // update user list connectorId in case it does not have it yet
                User user = userRepository.findById(userName).orElseThrow(() -> new IllegalStateException("User with userName " + userName + "does not exist."));
                if (user.getListConnectorId() == null) {
                    List<String> connectorList = new ArrayList<>();
                    connectorList.add(connector.getId());
                    user.setListConnectorId(connectorList);
                } else {
                    if (!user.getListConnectorId().contains(connector.getId()))
                        user.addConnectorId(connector.getId());
                }
                userRepository.save(user);
            }

        }
        return userConnectors;
    }

    public String addNewConnector(String userName, Bridge bridge, boolean inviteCollab) {
//        if (!verifyPmsExist(bridge))
//            throw new IllegalStateException("Cannot verify pms");
        Connector connector = new Connector(userName, bridge);
//        updateArtifactList(connector);

        User userPMage = userRepository.findById(connector.getUserName()).orElseThrow(()
                -> new IllegalStateException("User with username " + connector.getUserName() + "does not exist."));
        userPMage.addConnectorId(connector.getId());
        userRepository.save(userPMage);

//        if (inviteCollab)
//            if (userPMage.getRole().equals("manager")) {
//                List<String> collaborators = this.getProcessActors(bridge);
//
//            }

        connectorRepository.save(connector);
        return connector.getId();
    }

    public String addSupplementaryConnectors(Bridge bridge, String connectorId) {
        if (!verifyPmsExist(bridge))
            throw new IllegalStateException("Cannot verify pms");

        Connector baseConnector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        SupplementaryConnector suppConnector = new SupplementaryConnector(bridge, connectorId,
                baseConnector.getBridge().getAppName(),
                baseConnector.getBridge().getProjectDir(), baseConnector.getBridge().getProjectLink());
        updateArtifactList(suppConnector);

        User userPMage = userRepository.findById(suppConnector.getUserName()).orElseThrow(() -> new IllegalStateException("User with username " + suppConnector.getUserName() + "does not exist."));
        userPMage.addConnectorId(suppConnector.getId());
        userRepository.save(userPMage);

        assert suppConnectorRepository != null;
        suppConnectorRepository.save(suppConnector);

        return suppConnector.getId();
    }

    public String addSupplementaryConnectorsPast(Bridge bridge, String artifactList) {
        if (!verifyPmsExist(bridge))
            throw new IllegalStateException("Cannot verify pms");

        SupplementaryConnector suppConnector = new SupplementaryConnector(bridge, artifactList);
        updateArtifactList(suppConnector);

        User userPMage = userRepository.findById(suppConnector.getUserName()).orElseThrow(() -> new IllegalStateException("User with username " + suppConnector.getUserName() + "does not exist."));
        userPMage.addConnectorId(suppConnector.getId());
        userRepository.save(userPMage);

        assert suppConnectorRepository != null;
        suppConnectorRepository.save(suppConnector);

        return suppConnector.getId();
    }

    @Transactional
    public void updateConnector(String connectorId, Bridge bridge, String taskArtifact) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));
        connector.setBridge(bridge);

        connectorRepository.save(connector);
    }

    @Transactional
    public void updateSuppConnector(String connectorId, Bridge bridge, String baseConnectorId, String taskArtifact) {
        Connector baseConnector = connectorRepository.findById(baseConnectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        SupplementaryConnector connector = suppConnectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        connector.setBridge(bridge);
        connector.setSuppAppName(baseConnector.getBridge().getAppName());
        connector.setSuppProjectDir(baseConnector.getBridge().getProjectDir());
        connector.setSuppProjectLink(baseConnector.getBridge().getProjectLink());

        suppConnectorRepository.save(connector);
    }

    private boolean verifyPmsExist(Bridge bridge) {
        try {
            PmsConfig tmpConfig = new PmsConfig(bridge.getPMSConfig(), bridge.getPmsName());
            String finalUri = tmpConfig.callApi("verify", tmpConfig.getConfig(), bridge.toMap());
            return finalUri != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String getProcessInstance(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + " does not exist."));
        try {
            return connector.getPmsConfig().callApi("verify", connector.getPmsConfig().getConfig(), connector.getBridge().toMap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopMonitoringProcessInstance(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        connector.setMonitoring(false);
        connectorRepository.save(connector);
//        this.closeProcess(connector);
        System.out.println("Stop monitoring connector with id " + connectorId);
    }

    public void deleteConnector(String connectorId) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));

        User userPMage = userRepository.findById(connector.getUserName()).orElseThrow(() -> new IllegalStateException("User with username " + connector.getUserName() + "does not exist."));
        userPMage.removeConnectorId(connector.getId());
        userRepository.save(userPMage);

        connectorRepository.delete(connector);
    }

    public void addActionEventTable(String connectorId, String actionDescription) {
        Connector connector = connectorRepository.findById(connectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + connectorId + "does not exist."));
        connector.setActionEventDescription(actionDescription);

        List<ActionEvent> updateActionEventList = new ArrayList<>();
        String[] actionEventList = actionDescription.split("\n");

        if (actionEventList.length == 1)
            if (actionEventList[0].equals(""))
                return;

        for (String actionEventStr : actionEventList) {
            String[] actionEventToken = actionEventStr.split(",");
            ActionEvent actionEvent = new ActionEvent(actionEventToken);
            if (!updateActionEventList.contains(actionEvent))
                updateActionEventList.add(actionEvent);
        }
        Map<String, List<ActionEvent>> actionLinkage = new HashMap<>();
        actionLinkage.put("all", updateActionEventList);
        connector.setActionLinkage(actionLinkage);
        connectorRepository.save(connector);
    }

    public List<String> getTaskList(Connector connector ) {
        HttpClient client = HttpClients.createDefault();
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrl());
            urlMap.put("processInstanceId", connector.getBridge().getProcessInstanceId());

            String content = connector.getPmsConfig()
                    .callApi("getTask", connector.getPmsConfig().getConfig(), connector.getBridge().toMap());

            if (connector.getBridge().getPmsName().equals("core-bape")) {
                content = content.replace("[","").replace("]","").replace("\"","");
                return Arrays.asList(content.split(","));
            } else if (connector.getBridge().getPmsName().equals("bonita")) {
                JSONArray taskList = new JSONArray(content);

                List<String> taskNameList = new ArrayList<>();
                for (int i = 0; i< taskList.length(); i++) {
                    JSONObject task = taskList.getJSONObject(i);
                    String taskName = task.getString("name");
                    taskNameList.add(taskName);
                }
                return taskNameList;
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> extractKeywords(List<String> taskList) {
        this.loadProperties();
        StringBuilder taskListEntire = new StringBuilder();
        for (int i = 0; i < taskList.size(); i++) {
//            taskListEntire.append("end task ");
            taskListEntire.append(taskList.get(i));
            if (i < taskList.size() - 1)
                taskListEntire.append(" | ");
        }

        File directory = new File("./src/main/python");
        List<String> commands = new ArrayList<>();
//        commands.add("/home/nguyenminhkhoi/mambaforge/envs/fouille/bin/python");
//        commands.add("keyword_extract.py");
        String pythonPath = System.getProperty("python");
        commands.add(pythonPath);
        commands.add("text_transform.py");
        commands.add("\"" + taskListEntire + "\"");
        try {
            String summary = ExternalService.runCommand(directory, commands);
            return Arrays.asList(summary.split(";"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getArtifactList(String connectorId) {
        Connector connector = this.getConnector(connectorId);
        List<String> artifactList = new ArrayList<>();

        // TODO: create an API call to collect list of artifacts in an artifact centric process model
        return artifactList;
    }

    public String generateActionLinkageTest(Connector connector) {
        List<String> taskList = new ArrayList<>();
        if (connector.getUserName().equals("sunny"))
            taskList.addAll(Arrays.asList(new String[]{"T1", "T2"}));
        else if (connector.getUserName().equals("cherry"))
            taskList.addAll(Arrays.asList(new String[]{"T3", "T4", "T5"}));
        else if (connector.getUserName().equals("teddy"))
            taskList.addAll(Arrays.asList(new String[]{"T6", "T7", "T8"}));

        // read app event
        JSONObject appConfig = connector.getAppConfig().getConfig();
        JSONArray listAppEvent = appConfig.getJSONArray("event");

        // read pms event
        JSONObject pmsConfig = connector.getPmsConfig().getConfig();
        JSONArray listPmsEvent = pmsConfig.getJSONArray("api_info");

        List<ActionEvent> listActionEvent = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < listAppEvent.length(); j++) {
                for (int k = 0; k < listPmsEvent.length(); k++) {
                    // we skip the pms event not related to monitoring user behaviour
                    if (!listPmsEvent.getJSONObject(k).getBoolean("monitoring"))
                        continue;

                    String appEvent = listAppEvent.getJSONObject(j).get("name").toString();
                    String pmsEvent = listPmsEvent.getJSONObject(k).get("name").toString();
                    // updated : we should also include the pms event in the task list so that from the context info + app event
                    // we can infer the corresponding pms event and the task
                    ActionEvent actionEvent = new ActionEvent(appEvent, pmsEvent + " " + taskList.get(i), pmsEvent, taskList.get(i));
                    listActionEvent.add(actionEvent);
                }
            }
        }

        Map<String, List<ActionEvent>> actionEventMap = new HashMap<>();
        actionEventMap.put("all", listActionEvent);
        connector.setActionLinkage(actionEventMap);

        // generate the action linkage table
        StringBuilder actionLinkage = new StringBuilder();
        for (int i = 0; i < listActionEvent.size(); i++) {
            actionLinkage.append(listActionEvent.get(i).toString());
            if (i < listActionEvent.size() - 1)
                actionLinkage.append("\n");
        }

        connectorRepository.save(connector);
        return actionLinkage.toString();
    }

    public List<ActionEvent> generateActionLinkageEachArtifact(String artifact, Connector connector) {
        List<String> taskList = getTaskList(connector);

        // transfer the task list into NLP engine to get the keywords
        List<String> keywordList = extractKeywords(taskList);

        // read app event
        JSONObject appConfig = connector.getAppConfig().getConfig();
        JSONArray listAppEvent = appConfig.getJSONArray("event");

        // read pms event
        JSONObject pmsConfig = connector.getPmsConfig().getConfig();
        JSONArray listPmsEvent = pmsConfig.getJSONArray("api_info");

        List<ActionEvent> listActionEvent = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < listAppEvent.length(); j++) {
                for (int k = 0; k < listPmsEvent.length(); k++) {
                    // we skip the pms event not related to monitoring user behaviour
                    if (!listPmsEvent.getJSONObject(k).getBoolean("monitoring"))
                        continue;

                    String appEvent = listAppEvent.getJSONObject(j).get("name").toString();
                    String pmsEvent = listPmsEvent.getJSONObject(k).get("name").toString();
                    // updated : we should also include the pms event in the task list so that from the context info + app event
                    // we can infer the corresponding pms event and the task
                    ActionEvent actionEvent = new ActionEvent(appEvent, pmsEvent + " " + keywordList.get(i), pmsEvent, taskList.get(i));
                    listActionEvent.add(actionEvent);
                }
            }
        }

        return listActionEvent;
    }

    public String generateActionLinkage(String connectorId) {
        Connector connector = this.getConnector(connectorId);

        // call get process instance from the pms
        // get the task list
        connector.updateConfig();

        // handle artifact-centric process model
        Map<String, List<ActionEvent>> actionEventMap = new HashMap<>();
        if (connector.getPmsConfig().isArtifactCentric()) {
            List<String> artifactList = this.getArtifactList(connectorId);
            for(String artifact : artifactList)
                actionEventMap.put(artifact, generateActionLinkageEachArtifact(artifact, connector));
        } else
            actionEventMap.put("all", generateActionLinkageEachArtifact("all", connector));

        connector.setActionLinkage(actionEventMap);

        // generate the action linkage table
        StringBuilder actionLinkage = new StringBuilder();
        for (String artifact : actionEventMap.keySet()) {
            List<ActionEvent> listActionEvent = actionEventMap.get(artifact);
            // ignore adding the name of the artifact if it's "all", i.e. activity centric process model
            if (!artifact.equals("all"))
                actionLinkage.append(artifact).append("\n");
            for (int i = 0; i < listActionEvent.size(); i++) {
                actionLinkage.append(listActionEvent.get(i).toString());
                if (i < listActionEvent.size() - 1)
                    actionLinkage.append("\n");
            }
        }

        connectorRepository.save(connector);
        return actionLinkage.toString();
    }

    public void loadProperties() {
        try {
            InputStream file = Connector.class.getResourceAsStream("/application.properties");
            if (file!=null) System.getProperties().load(file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public List<String> getPMSList() {
        this.loadProperties();
        PMSConfigManager pmsManager = new PMSConfigManager(System.getProperty("pmsconfig"));
        return pmsManager.getListPMSName();
    }

    public List<String> getAppList() {
        this.loadProperties();
        AppConfigManager appManager = new AppConfigManager(System.getProperty("appconfig"));
        return appManager.getListAppName();
    }

    public String addPMSConfig(String pmsConfig) {
        this.loadProperties();
        PMSConfigManager pmsManager = new PMSConfigManager(System.getProperty("pmsconfig"));
        return pmsManager.addPMSConfig(pmsConfig);
    }

    public List<Alignment> getConnectorHist(String baseConnectorId) {
        Connector baseConnector = connectorRepository.findById(baseConnectorId).orElseThrow(() -> new IllegalStateException("Connector with id " + baseConnectorId + " does not exist."));

        return baseConnector.getHistoryTriggerList();
    }

    public void updateArtifactList(Connector connector) {
        if (connector.getBridge().getPmsName().equals("bonita")) {
            for (TaskArtifact taskArtifact: connector.getTaskArtifactList()) {
                for (Artifact input: taskArtifact.getInput()) {
                    if (!connector.getArtifactPool().containsKey(input.getName())) {
                        connector.addArtifact(input);
                    }
                }

                for (Artifact output: taskArtifact.getOutput()) {
                    if (!connector.getArtifactPool().containsKey(output.getName())) {
                        connector.addArtifact(output);
                    }
                }
            }
        }

        HttpClient client = HttpClients.createDefault();
        try {
            Map<String, String> urlMap = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();

            urlMap.put("url", connector.getPmsConfig().getUrl());
            urlMap.put("processInstanceId", connector.getBridge().getProcessInstanceId());

            String finalUri = connector.getPmsConfig()
                    .callApi("getArtifact", connector.getPmsConfig().getConfig(), connector.getBridge().toMap());
            HttpGet getMethod = new HttpGet(finalUri);

            // for early development only
            // TODO: make it generic for all other PMSs
            HttpResponse getResponse = client.execute(getMethod);

            int getStatusCode = getResponse.getStatusLine()
                    .getStatusCode();
            if (getStatusCode == 200) {
                String content = EntityUtils.toString(getResponse.getEntity());

                if (connector.getBridge().getPmsName().equals("core-bape")) {
                    JSONArray taskList = new JSONArray(content);

                    for (int i = 0; i < taskList.length(); i++) {
                        JSONObject task = taskList.getJSONObject(i);
                        String artifactName = task.getString("name");
                        String artifactState = task.getString("state");
                        connector.addArtifact(new Artifact(artifactName, artifactState, false));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSuggestedApp(String processName) {
        this.loadProperties();
        System.out.println(processName);
        File directory = new File("./src/main/python");
        List<String> commands = new ArrayList<>();
        String pythonPath = System.getProperty("python");
        commands.add(pythonPath);
        commands.add("suggest_app.py");
        commands.add("\"" + processName + "\"");
        try {
            String appSuggested = ExternalService.runCommand(directory, commands);
            System.out.println(appSuggested);
            return appSuggested;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
