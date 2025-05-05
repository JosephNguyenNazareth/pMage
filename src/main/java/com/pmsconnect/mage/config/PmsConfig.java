package com.pmsconnect.mage.config;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PmsConfig {
    private String pms;
    private JSONObject config;
    private String configPath;
    private final Map<String, Map<String, String>> returnValues = new HashMap<>();

    public PmsConfig() {
        System.out.println("hello");
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
        if (apiConfig.has("header") && apiConfig.getJSONObject("header").has("dynamic")) {
            JSONObject dynamicHeaders = apiConfig.getJSONObject("header").getJSONObject("dynamic");
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
    }

    private String callApi(String apiName, JSONObject apiConfig, Map<String, String> inputValues) throws IOException {
        String method = apiConfig.getString("method");
        String urlStr = replacePlaceholders(apiConfig.getString("url"), inputValues);

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

        // POST body
        if ("POST".equalsIgnoreCase(method) && apiConfig.has("body")) {
            conn.setDoOutput(true);
            JSONArray bodyKeys = apiConfig.getJSONArray("body");
            String bodyData = bodyKeys.toList().stream()
                    .map(Object::toString)
                    .map(k -> {
                try {
                    return k + "=" + URLEncoder.encode(inputValues.getOrDefault(k, ""), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            })
                    .collect(Collectors.joining("&"));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyData.getBytes());
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
            for (String key : ret.keySet()) {
                String source = ret.getJSONObject(key).getString("header");
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
