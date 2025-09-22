package com.pmsconnect.mage.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PmsConfig {
    private String pms;
    private JSONObject config;
    private String configPath;
    private Map<String, Map<String, Object>> returnValues = new HashMap<>();

    public PmsConfig() {
    }

    public PmsConfig(String configPath) {
        this.configPath = configPath;
    }

    public PmsConfig(String configPath, String pms) {
        this.configPath = configPath;
        this.pms = pms;
        this.readConfig();
    }

    public PmsConfig(String configPath, String pms, JSONObject config) {
        this.configPath = configPath;
        this.pms = pms;
        this.config = config;
    }

    public String getPms() {
        return pms;
    }

    public void setPms(String pms) {
        this.pms = pms;
    }

    public void resetReturnedValues() {
        this.returnValues = new HashMap<>();
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

    public Map<String, Map<String, Object>> getReturnValues() {
        return returnValues;
    }

    public boolean isArtifactCentric() {
        return this.config.getBoolean("artifact-centric");
    }

    public void readConfig() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(this.configPath)));
            JSONArray configList = new JSONArray(content);
            for (int i = 0; i < configList.length(); i++) {
                JSONObject configPms = configList.getJSONObject(i);
                if (configPms.getString("pms").equals(this.pms)){
                    this.config = configPms;
                    return;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return this.config.getString("url");
    }

    public String callApiWithDependencies(String apiName, Map<String, String> inputValues) throws IOException {
        JSONObject apiConfig = findApiConfig(apiName);
        if (apiConfig == null) throw new IllegalArgumentException("API not found: " + apiName);

        resolveDependencies(apiConfig, inputValues);

        return callApi(apiName, apiConfig, inputValues);
    }

    public String callApiWithDependencies(String apiName, Map<String, String> inputValues, Map<String, String> condValues) throws IOException {
        JSONObject apiConfig = findApiConfig(apiName);
        if (apiConfig == null) throw new IllegalArgumentException("API not found: " + apiName);

        resolveDependencies(apiConfig, inputValues);

        return callApi(apiName, apiConfig, inputValues, condValues);
    }

    private void resolveDependencies(JSONObject apiConfig, Map<String, String> inputValues) throws IOException {
        // Resolve dynamic headers
        if (!apiConfig.has("require"))
            return;
        JSONObject requirement = apiConfig.getJSONObject("require");

        if (requirement.has("header") && requirement.getJSONObject("header").has("dynamic")) {
            JSONObject dynamicHeaders = requirement.getJSONObject("header").getJSONObject("dynamic");
            for (String headerKey : dynamicHeaders.keySet()) {
                JSONObject dependencyInfo = dynamicHeaders.getJSONObject(headerKey);
                for (String depApi : dependencyInfo.keySet()) {
                    if (!returnValues.containsKey(depApi)) {
                        callApiWithDependencies(depApi, inputValues);
                    }
                }
            }
        }

        // Resolve dynamic cookies
        if (apiConfig.has("cookie") && apiConfig.getJSONObject("cookie").has("dynamic")) {
            JSONObject dynamicCookies = apiConfig.getJSONObject("cookie").getJSONObject("dynamic");
            for (String cookieKey : dynamicCookies.keySet()) {
                JSONObject dependencyInfo = dynamicCookies.getJSONObject(cookieKey);
                for (String depApi : dependencyInfo.keySet()) {
                    if (!returnValues.containsKey(depApi)) {
                        callApiWithDependencies(depApi, inputValues);
                    }
                }
            }
        }

        // Resolve parameters
//        if (apiConfig.has("param")) {
//            JSONArray listParams = apiConfig.getJSONArray("param");
//            for (int i = 0; i < listParams.length(); i++) {
//                String paramValue = listParams.get(i).toString();
//                for (String depApi : dependencyInfo.keySet()) {
//                    if (!returnValues.containsKey(depApi)) {
//                        callApiWithDependencies(depApi, inputValues);
//                    }
//                }
//            }
//        }

        // Resolve path
        if (requirement.has("path") && requirement.getJSONObject("path").has("dynamic")) {
            JSONObject dynamicPathVar = requirement.getJSONObject("path").getJSONObject("dynamic");
            for (String pathVarKey : dynamicPathVar.keySet()) {
                JSONObject dependencyInfo = dynamicPathVar.getJSONObject(pathVarKey);
                for (String depApi : dependencyInfo.keySet()) {
                    if (!returnValues.containsKey(depApi)) {
                        JSONObject returnValueInfo = dependencyInfo.getJSONObject(depApi);
                        if (returnValueInfo.has("type")) {
                            if (returnValueInfo.getString("type").equals("return-loop")) {
                                Map<String, String> condRetrieval = new HashMap<>();
                                for (String cond : returnValueInfo.keySet()) {
                                    if (!cond.equals("type"))
                                        condRetrieval.put(cond, returnValueInfo.getString(cond));
                                }
                                callApiWithDependencies(depApi, inputValues, condRetrieval);
                            }
                        } else
                            callApiWithDependencies(depApi, inputValues);
                    }
                }
            }
        }
    }

    public String callApi(String apiName, JSONObject apiConfig, Map<String, String> inputValues) throws IOException {
        return callApi(apiName, apiConfig, inputValues, null);
    }

    public String callApi(String apiName, JSONObject apiConfig, Map<String, String> inputValues, Map<String, String> condValues) throws IOException {
        String method = apiConfig.getString("method");
        String urlStr = replacePlaceholders(apiConfig.getString("url"), inputValues);

        // Handle dynamic path parameters
        if (apiConfig.has("require") && apiConfig.getJSONObject("require").has("path")
                && apiConfig.getJSONObject("require").getJSONObject("path").has("dynamic")) {
            JSONObject pathDynamic = apiConfig.getJSONObject("require").getJSONObject("path").getJSONObject("dynamic");

            for (String pathParam : pathDynamic.keySet()) {
                JSONObject paramConfig = pathDynamic.getJSONObject(pathParam);

                for (String sourceApi : paramConfig.keySet()) {
                    JSONObject sourceConfig = paramConfig.getJSONObject(sourceApi);
                    String type = sourceConfig.getString("type");

                    if ("return-loop".equals(type)) {
                        // Get filter criteria from the source config
                        String filterField = null;
                        String filterValue = null;

                        for (String key : sourceConfig.keySet()) {
                            if (!"type".equals(key)) {
                                filterField = key;
                                filterValue = inputValues.get(sourceConfig.getString(key));
                                break;
                            }
                        }

                        // Get the required value from the source API's return values
                        String paramValue = getFilteredReturnValue(sourceApi, pathParam, filterField, filterValue);

                        if (paramValue != null) {
                            urlStr = urlStr.replace("{" + pathParam + "}", paramValue);
                        }
                    }
                }
            }
        }

        // If method has params, append to URL
        if (apiConfig.has("param")) {
            JSONArray paramKeys = apiConfig.getJSONArray("param");
            List<String> params = new ArrayList<>();
            for (int i = 0; i < paramKeys.length(); i++) {
                String key = paramKeys.getString(i);
                String keyAlt = key;

                // TODO: having a mapping table to convert parameters of PMS to keywords in pMage
                // by now, just put if else condition
                if (key.equals("actorName"))
                    keyAlt = "userNameApp";
                else if (key.equals("taskName"))
                    keyAlt = "task";

                String value = URLEncoder.encode(inputValues.getOrDefault(keyAlt, ""), "UTF-8");
                params.add(key + "=" + value);
            }
            String paramData = String.join("&", params);

            if (params.size() > 0)
                urlStr += "?" + paramData;
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);

        // Handle authorization
        if (apiConfig.has("require") && apiConfig.getJSONObject("require").has("authorization")) {
            JSONObject auth = apiConfig.getJSONObject("require").getJSONObject("authorization");
            if (auth.has("dynamic")) {
                JSONObject authDynamic = auth.getJSONObject("dynamic");
                String username = inputValues.get(authDynamic.getString("Username"));
                String password = inputValues.get(authDynamic.getString("Password"));
                String authType = authDynamic.getString("Auth Type");

                if ("Basic Auth".equals(authType)) {
                    String credentials = username + ":" + password;
                    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                    conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
                }
            }
        }

        // Fixed headers
        if (apiConfig.has("header") && apiConfig.getJSONObject("header").has("fixed")) {
            JSONObject fixed = apiConfig.getJSONObject("header").getJSONObject("fixed");
            for (String key : fixed.keySet()) {
                conn.setRequestProperty(key, fixed.getString(key));
            }
        }

        // Dynamic headers
        if (apiConfig.has("require")) {
            if (apiConfig.getJSONObject("require").has("header") && apiConfig.getJSONObject("require").getJSONObject("header").has("dynamic")) {
                JSONObject dynamicHeader = apiConfig.getJSONObject("require").getJSONObject("header").getJSONObject("dynamic");
                for (String key : dynamicHeader.keySet()) {
                    JSONObject fromApi = dynamicHeader.getJSONObject(key);
                    for (String sourceApi : fromApi.keySet()) {
                        conn.setRequestProperty(key, returnValues.get(sourceApi).get(key).toString());
                    }
                }
            }
        }

        // Dynamic cookies
        if (apiConfig.has("require")) {
            if (apiConfig.getJSONObject("require").has("cookie") && apiConfig.getJSONObject("require").getJSONObject("cookie").has("dynamic")) {
                JSONObject dynamic = apiConfig.getJSONObject("require").getJSONObject("cookie").getJSONObject("dynamic");
                List<String> cookies = new ArrayList<>();
                for (String key : dynamic.keySet()) {
                    JSONObject fromApi = dynamic.getJSONObject(key);
                    for (String sourceApi : fromApi.keySet()) {
                        cookies.add(key + "=" + returnValues.get(sourceApi).get(key).toString());
                    }
                }
                conn.setRequestProperty("Cookie", String.join("; ", cookies));
            }
        }

        // POST/PUT body or param
        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && apiConfig.has("body")) {
            conn.setDoOutput(true);
            StringBuilder bodyBuilder = new StringBuilder();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            JSONObject bodyFields = apiConfig.getJSONObject("body");
            for (String key : bodyFields.keySet()) {
                bodyBuilder.append(key).append("=").append(inputValues.getOrDefault(bodyFields.getString(key), ""));
                bodyBuilder.append("&");
            }

            bodyBuilder.append("redirect=false");
            String urlBody = bodyBuilder.toString();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(urlBody.getBytes());
            }
        }

        // Read response
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

        // Store return values
        if (apiConfig.has("return")) {
            Map<String, Object> returned = new HashMap<>();
            JSONObject ret = apiConfig.getJSONObject("return");

            // Parse the response as JSONObject
            JSONObject responseObj;
            try {
                responseObj = new JSONObject(response);
            } catch (Exception e) {
                // If parsing fails, create empty object and skip processing
                responseObj = new JSONObject();
            }

            // Store the raw response for filtering purposes
            returned.put("_responseData", responseObj);

            for (String key : ret.keySet()) {
                JSONObject keyConfig = ret.getJSONObject(key);

                if (keyConfig.has("header")) {
                    Map<String, List<String>> headerFields = conn.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get("Set-Cookie");

                    String value = null;

                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            if (cookie.startsWith(key)) {
                                value = cookie.split(";", 2)[0].split("=")[1];
                                break;
                            }
                        }
                    }
//                    String source = keyConfig.getString("header");
//                    String raw = conn.getHeaderField(source);
//                    if (raw != null) {
//                        String value = Arrays.stream(raw.split(";"))
//                                .filter(s -> s.trim().startsWith(key + "="))
//                                .map(s -> s.trim().substring((key + "=").length()))
//                                .findFirst().orElse(null);
                        if (value != null) {
                            returned.put(key, value);
                        }
                    //}
                } else if (keyConfig.has("result")) {

                    String jsonKey = keyConfig.getString("result");

                    // Handle nested response structures
                    JSONArray dataArray = getDataArray(responseObj, apiName);
                    if (dataArray != null && dataArray.length() > 0) {
                        List<String> values = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject item = dataArray.getJSONObject(i);
                            if (item.has(jsonKey)) {
                                values.add(item.get(jsonKey).toString());
                            }
                        }
                        returned.put(key, values);
                    } else if (responseObj.has(jsonKey)) {
                        // Direct field access for single object responses
                        returned.put(key, responseObj.get(jsonKey).toString());
                    }
                } else if (keyConfig.has("body")) {
                    String jsonKey = keyConfig.getString("body");

                    // Handle nested response structures
                    JSONArray dataArray = getDataArray(responseObj, apiName);
                    if (dataArray != null && dataArray.length() > 0) {
                        List<String> values = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject item = dataArray.getJSONObject(i);
                            if (item.has(jsonKey)) {
                                values.add(item.get(jsonKey).toString());
                            }
                        }
                        returned.put(key, values);
                    } else if (responseObj.has(jsonKey)) {
                        // Direct field access for single object responses
                        returned.put(key, responseObj.get(jsonKey).toString());
                    }
                }
            }
            returnValues.put(apiName, returned);
        }

        return response;
    }

    // Helper method to get filtered return value from previous API calls
    private String getFilteredReturnValue(String sourceApi, String targetField, String filterField, String filterValue) {
        if (!returnValues.containsKey(sourceApi)) {
            return null;
        }

        Map<String, Object> sourceReturns = returnValues.get(sourceApi);

        // Check if we have stored response data for filtering
        if (sourceReturns.containsKey("_responseData")) {
            Object responseData = sourceReturns.get("_responseData");

            if (responseData instanceof JSONObject) {
                JSONObject responseObj = (JSONObject) responseData;
                JSONArray dataArray = getDataArray(responseObj, sourceApi);

                if (dataArray != null) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);

                        // Check if this item matches our filter criteria
                        String mappedFilterField = getFieldMapping(filterField);
                        if (item.has(mappedFilterField) &&
                                filterValue != null && filterValue.equals(item.get(mappedFilterField).toString())) {

                            // Return the target field value from this item
                            String mappedTargetField = getFieldMapping(targetField);
                            if (item.has(mappedTargetField)) {
                                return item.get(mappedTargetField).toString();
                            }
                        }
                    }
                }
            }
        }

        // Fallback: try to get from stored return values directly
        if (sourceReturns.containsKey(targetField)) {
            Object value = sourceReturns.get(targetField);
            if (value instanceof List) {
                // If it's a list, return the first item for now
                List<?> list = (List<?>) value;
                return list.isEmpty() ? null : list.get(0).toString();
            } else {
                return value.toString();
            }
        }

        return null;
    }

    // Helper method to get the data array from API responses based on API name
    private JSONArray getDataArray(JSONObject responseObj, String apiName) {
        try {
            if ("getTaskInstances".equals(apiName) && responseObj.has("task-summary")) {
                return responseObj.getJSONArray("task-summary");
            } else if ("getProcessInstances".equals(apiName) && responseObj.has("process-instance")) {
                return responseObj.getJSONArray("process-instance");
            } else if ("getTask".equals(apiName) && responseObj.has("task")) {
                return responseObj.getJSONArray("task");
            }
            // Add more API-specific mappings as needed

            // Generic fallback - look for common array field names
            String[] commonArrayFields = {"items", "data", "results", "list"};
            for (String fieldName : commonArrayFields) {
                if (responseObj.has(fieldName)) {
                    Object field = responseObj.get(fieldName);
                    if (field instanceof JSONArray) {
                        return (JSONArray) field;
                    }
                }
            }
        } catch (Exception e) {
            // Return null if any parsing fails
        }
        return null;
    }

    // Helper method to map configuration field names to actual API response field names
    private String getFieldMapping(String configField) {
        switch (configField) {
            case "processName":
            case "process-name":
                return "process-name";
            case "task":
            case "task-name":
                return "task-name";
            case "taskInstanceId":
            case "task-id":
                return "task-id";
            case "containerId":
            case "container-id":
                return "container-id";
            case "processId":
            case "process-id":
                return "process-id";
            case "processInstanceId":
            case "process-instance-id":
                return "process-instance-id";
            default:
                return configField;
        }
    }

    private JSONObject findApiConfig(String apiName) {
        JSONObject apiInfo = config.getJSONObject("api_info");

        if (!apiInfo.has(apiName)) {
            System.err.println("API configuration for '" + apiName + "' not found");
            return null;
        }

        return apiInfo.getJSONObject(apiName);
    }

    private String replacePlaceholders(String input, Map<String, String> values) {
        String result = input;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
