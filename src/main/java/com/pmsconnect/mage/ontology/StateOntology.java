package com.pmsconnect.mage.ontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.pmsconnect.mage.project.coordination.ActivityState;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.opencsv.exceptions.CsvException;

public class StateOntology {
    private Map<ActivityState, List<String>> stateSynonyms;
    private Map<ActivityState, Map<String, String>> pmsState;
    private Map<ActivityState, String> correspondingAPI;

    public StateOntology() throws IOException, CsvException {
        this.stateSynonyms = new HashMap<>();
        this.pmsState = new HashMap<>();
        this.correspondingAPI = new HashMap<>();
        this.loadAllData("./src/main/resources/state_synonyms.csv", "./src/main/resources/state_action.csv", "./src/main/resources/pms_config.json");
    }

    public void loadAllData(String synonymsCsvPath, String apiCsvPath,
                            String pmsStateMappingJsonPath) throws IOException, CsvException {
        readStateSynonymsCsv(synonymsCsvPath);
        readStateAPICallCsv(apiCsvPath);
        readPmsApiMappingJson(pmsStateMappingJsonPath);
    }

    public void readStateSynonymsCsv(String csvFilePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> records = reader.readAll();

            // Skip header row
            boolean hasHeader = true;
            for (String[] record : records) {
                if (hasHeader) {
                    hasHeader = false;
                    continue;
                }

                if (record.length >= 2) {
                    String activityStateStr = record[0].trim();
                    String synonym = record[1].trim();
                    ActivityState activityState = ActivityState.valueOf(activityStateStr.toUpperCase());
                    this.stateSynonyms.computeIfAbsent(activityState, k -> new ArrayList<>()).add(synonym);
                }
            }
        }
    }

    public void readStateAPICallCsv(String csvFilePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> records = reader.readAll();

            // Skip header row
            boolean hasHeader = true;
            for (String[] record : records) {
                if (hasHeader) {
                    hasHeader = false;
                    continue;
                }

                if (record.length >= 2) {
                    String activityStateStr = record[0].trim();
                    String apiFunction = record[1].trim();
                    ActivityState activityState = ActivityState.valueOf(activityStateStr.toUpperCase());
                    this.correspondingAPI.computeIfAbsent(activityState, k -> apiFunction);
                }
            }
        }
    }

    public void readPmsApiMappingJson(String jsonFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (FileReader reader = new FileReader(jsonFilePath)) {
            JsonNode rootNode = mapper.readTree(reader);

            if (rootNode.isArray()) {
                for (JsonNode pmsNode : rootNode) {
                    String pmsName = pmsNode.get("pms").asText();

                    // Parse state mapping
                    JsonNode stateMappingNode = pmsNode.get("state_mapping");
                    if (stateMappingNode != null) {
                        stateMappingNode.fields().forEachRemaining(entry -> {
                            try {
                                ActivityState activityState = ActivityState.valueOf(entry.getKey());
                                String concreteState = entry.getValue().asText();
                                this.pmsState.computeIfAbsent(activityState, k -> new HashMap<>()).put(pmsName, concreteState);
                            } catch (IllegalArgumentException e) {
                                System.err.println("Invalid ActivityState: " + entry.getKey());
                            }
                        });
                    }
                }
            }
        }
    }

    public Map<ActivityState, List<String>> getStateSynonyms() {
        return stateSynonyms;
    }

    public void setStateSynonyms(Map<ActivityState, List<String>> stateSynonyms) {
        this.stateSynonyms = stateSynonyms;
    }

    public Map<ActivityState, Map<String, String>> getPmsState() {
        return pmsState;
    }

    public void setPmsState(Map<ActivityState, Map<String, String>> pmsState) {
        this.pmsState = pmsState;
    }

    public Map<ActivityState, String> getCorrespondingAPI() {
        return correspondingAPI;
    }

    public void setCorrespondingAPI(Map<ActivityState, String> correspondingAPI) {
        this.correspondingAPI = correspondingAPI;
    }

    public ActivityState getActivityStateFromFunction(String functionCall) {
        for (ActivityState activityState : this.correspondingAPI.keySet()) {
            if (this.correspondingAPI.get(activityState).equals(functionCall))
                return activityState;
        }
        return null;
    }
}
