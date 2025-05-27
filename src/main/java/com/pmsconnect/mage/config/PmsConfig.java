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
    private Map<String, Map<String, String>> returnValues = new HashMap<>();

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

    public Map<String, Map<String, String>> getReturnValues() {
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
        if (apiConfig.has("param") && apiConfig.getJSONObject("param").has("dynamic")) {
            JSONObject dynamicParams = apiConfig.getJSONObject("param").getJSONObject("dynamic");
            for (String paramKey : dynamicParams.keySet()) {
                JSONObject dependencyInfo = dynamicParams.getJSONObject(paramKey);
                for (String depApi : dependencyInfo.keySet()) {
                    if (!returnValues.containsKey(depApi)) {
                        callApiWithDependencies(depApi, inputValues);
                    }
                }
            }
        }

    }

    public String callApi(String apiName, JSONObject apiConfig, Map<String, String> inputValues) throws IOException {
        String method = apiConfig.getString("method");
        String urlStr = replacePlaceholders(apiConfig.getString("url"), inputValues);

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

            urlStr += "?" + paramData;
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);

        // Fixed headers
        if (apiConfig.has("header") && apiConfig.getJSONObject("header").has("fixed")) {
            JSONObject fixed = apiConfig.getJSONObject("header").getJSONObject("fixed");
            for (String key : fixed.keySet()) {
                conn.setRequestProperty(key, fixed.getString(key));
            }
        }

        // Dynamic headers
        if (apiConfig.has("header") && apiConfig.getJSONObject("header").has("dynamic")) {
            JSONObject dynamic = apiConfig.getJSONObject("header").getJSONObject("dynamic");
            for (String key : dynamic.keySet()) {
                JSONObject fromApi = dynamic.getJSONObject(key);
                for (String sourceApi : fromApi.keySet()) {
                    conn.setRequestProperty(key, returnValues.get(sourceApi).get(key));
                }
            }
        }

        // Dynamic cookies
        if (apiConfig.has("cookie") && apiConfig.getJSONObject("cookie").has("dynamic")) {
            JSONObject dynamic = apiConfig.getJSONObject("cookie").getJSONObject("dynamic");
            List<String> cookies = new ArrayList<>();
            for (String key : dynamic.keySet()) {
                JSONObject fromApi = dynamic.getJSONObject(key);
                for (String sourceApi : fromApi.keySet()) {
                    cookies.add(key + "=" + returnValues.get(sourceApi).get(key));
                }
            }
            conn.setRequestProperty("Cookie", String.join("; ", cookies));
        }

        // POST/PUT body or param
        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && apiConfig.has("body")) {
            conn.setDoOutput(true);
            StringBuilder bodyBuilder = new StringBuilder();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            JSONObject jsonBody = new JSONObject();
            JSONArray bodyFields = apiConfig.getJSONArray("body");
            for (Object keyObj : bodyFields) {
                String key = keyObj.toString();
                jsonBody.put(key, inputValues.getOrDefault(key, ""));
            }

            if (bodyBuilder.length() > 0) {
                bodyBuilder.append("&");
            }
            bodyBuilder.append(jsonBody.toString());


            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBuilder.toString().getBytes());
            }
        }

        // Read response
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

        // Store return values
        if (apiConfig.has("return")) {
            Map<String, String> returned = new HashMap<>();
            JSONObject ret = apiConfig.getJSONObject("return");

            // Parse the body as JSONObject
            JSONObject bodyJson = new JSONObject(response);

            for (String key : ret.keySet()) {
                JSONObject keyConfig = ret.getJSONObject(key);

                if (apiConfig.has("header")) {
                    String source = keyConfig.getString("header");
                    String raw = conn.getHeaderField(source);
                    if (raw != null) {
                        String value = Arrays.stream(raw.split(";"))
                                .filter(s -> s.trim().startsWith(key + "="))
                                .map(s -> s.trim().substring((key + "=").length()))
                                .findFirst().orElse(null);
                        if (value != null) {
                            returned.put(key, value);
                        }
                    }
                } else if (keyConfig.has("body")) {
                    String jsonKey = keyConfig.getString("body");
                    if (bodyJson.has(jsonKey)) {
                        returned.put(key, bodyJson.get(jsonKey).toString());
                    }
                }
            }
            returnValues.put(apiName, returned);
        }

        return response;
    }

    private JSONObject findApiConfig(String apiName) {
        JSONArray apis = config.getJSONArray("api_info");
        for (int i = 0; i < apis.length(); i++) {
            JSONObject entry = apis.getJSONObject(i);
            if (entry.getString("name").equals(apiName)) {
                return entry.getJSONObject("config");
            }
        }
        return null;
    }

    private String replacePlaceholders(String input, Map<String, String> values) {
        String result = input;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
