package com.pmsconnect.mage.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.user.Bridge;
import com.pmsconnect.mage.utils.ActionEvent;
import com.pmsconnect.mage.utils.LogPattern;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

public class AppConfig {
    private String projectLink;
    private JSONObject config;
    private String configPath;
    private String app;

    public AppConfig() {

    }

    public AppConfig(String configPath) {
        this.configPath = configPath;
        this.readConfig();
    }

    public AppConfig(String configPath, String projectLink) {
        this.configPath = configPath;
        this.projectLink = projectLink;
        this.readConfig();
    }

    public AppConfig(String configPath, String projectLink, JSONObject config) {
        this.projectLink = projectLink;
        this.config = config;
        this.configPath = configPath;
        this.readConfig();
    }

    public String getProjectLink() {
        return projectLink;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }

    public void readConfig() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(this.configPath)));
            JSONArray configList = new JSONArray(content);
            for (int i = 0; i < configList.length(); i++) {
                JSONObject configApp = configList.getJSONObject(i);
                if (this.projectLink.contains(configApp.getString("app"))){
                    this.config = configApp;
                    this.app = configApp.getString("app");
                    return;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject getAppFromLink(String projectLink) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(this.configPath)));
            JSONArray configList = new JSONArray(content);
            for (int i = 0; i < configList.length(); i++) {
                JSONObject configApp = configList.getJSONObject(i);
                if (projectLink.contains(configApp.getString("app"))){
                   return configApp;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> emitAppEvent(Connector connector, String appEvent, List<String> contextInfoList) {
        JSONArray appActions = this.config.getJSONArray("event");

        for (int i = 0; i < appActions.length(); i++) {
            JSONObject action = appActions.getJSONObject(i);
            if (!action.getString("name").equals(appEvent)) continue;

            String method = action.getString("method");
            String location = resolveLocation(action.getString("apiInfo"), connector);

            switch (method) {
                case "LOG":
                    return handleLogEvent(location, action.getString("important"), contextInfoList);

                case "GET":
                    return handleGetEvent(location, action.getJSONObject("important"), contextInfoList, connector);

                default:
                    // Optional: log unsupported method
                    break;
            }
        }
        return null;
    }

    private String resolveLocation(String location, Connector connector) {
        return location
                .replace("{userNameApp}", connector.getBridge().getUserNameApp())
                .replace("{projectName}", connector.getBridge().getProcessDef());
    }

    private Map<String, String> handleLogEvent(String location, String template, List<String> contextInfoList) {
        LogPattern logPattern = new LogPattern(template);

        try {
            List<String> logLines = Files.readAllLines(new File(location).toPath(), Charsets.UTF_8);
            for (String log : logLines) {
                Matcher matcher = logPattern.getPattern().matcher(log);
                if (matcher.matches()) {
                    Map<String, String> extracted = extractMatchedGroups(matcher, logPattern);
                    if (contextInfoList.contains(extracted.get("task"))) {
                        return extracted;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Consider logging framework
        }
        return null;
    }

    private Map<String, String> handleGetEvent(String location, JSONObject template, List<String> contextInfoList, Connector connector) {
        Object appEventData = this.callAPI(location, this.config, connector.getBridge());

        if (appEventData instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) appEventData;
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject obj = jsonArray.getJSONObject(j);
                Map<String, String> result = tryExtractMatch(obj, template, contextInfoList);
                if (result != null) return result;
            }
        } else if (appEventData instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) appEventData;
            return tryExtractMatch(jsonObj, template, contextInfoList);
        }
        return null;
    }

    private Map<String, String> tryExtractMatch(JSONObject obj, JSONObject template, List<String> contextInfoList) {
        try {
            Map<String, String> fields = this.extractFields(obj, template);
            if (contextInfoList.contains(fields.get("task"))) {
                return fields;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // Consider handling more gracefully
        }
        return null;
    }

    private Map<String, String> extractMatchedGroups(Matcher matcher, LogPattern logPattern) {
        Map<String, String> extracted = new LinkedHashMap<>();
        List<String> groupNames = logPattern.getGroupNames();
        for (int i = 0; i < groupNames.size(); i++) {
            extracted.put(groupNames.get(i), matcher.group(i + 1));
        }
        return extracted;
    }


    private JSONArray callAPI(String apiLink, JSONObject currentConfig, Bridge bridge) {
        HttpClient client = HttpClients.createDefault();
        URIBuilder builder = null;

        // assuming all the application having the same authentication protocol ???
        String auth = bridge.getUserNameApp() + ":" + bridge.getPasswordApp();
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        try {
            builder = new URIBuilder(apiLink);
            String finalUri = builder.build().toString();

            int postStatusCode = -1;
            HttpResponse response = null;
            if (currentConfig.getString("method").equals("GET")) {
                HttpGet getMethod = new HttpGet(finalUri);
                getMethod.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                response = client.execute(getMethod);

                postStatusCode = response.getStatusLine()
                        .getStatusCode();
            }

            if (postStatusCode != 200)
                return null;
            else {
                String result = EntityUtils.toString(response.getEntity());
                return new JSONArray(result);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> extractFields(JSONObject jsonObject, JSONObject jsonTemplate) throws JsonProcessingException {
        // transform the data template from JSON to map
        Map<String, String> template = new HashMap<>();
        Iterator<String> keys = jsonTemplate.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            template.put(key, jsonTemplate.get(key).toString());
        }

        // create the path for reading the data from extracted template
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonObject.toString());

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : template.entrySet()) {
            String outputKey = entry.getKey();
            String path = entry.getValue();
            String[] parts = path.split("\\|");

            JsonNode current = jsonNode;
            for (String part : parts) {
                if (current != null)
                    current = current.get(part);
                else
                    break;
            }
            result.put(outputKey, current != null ? current.asText() : null);
        }

        return result;
    }
}
