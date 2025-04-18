package com.pmsconnect.mage.utils.lock;

import java.util.*;

public class GraphForPMage {
    private Map<String, List<String>> adjacencyList;

    public GraphForPMage(){
        this.adjacencyList = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        this.adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }

    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(Map<String, List<String>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public String[][] asMatrix() {
        List<String[]> edges = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                edges.add(new String[]{from, to});
            }
        }

        return edges.toArray(new String[0][]);
    }

    public void combine (List<GraphForPMage> graphForPMageList) {
        for (GraphForPMage fragment : graphForPMageList) {
            for (Map.Entry<String, List<String>> entry : fragment.getAdjacencyList().entrySet()) {
                this.adjacencyList.merge(entry.getKey(), entry.getValue(), (list1, list2) -> {
                    Set<String> set = new LinkedHashSet<>(list1);
                    set.addAll(list2); // Only unique values are kept
                    return new ArrayList<>(set);
                });
            }
        }
    }

    public void printGraph() {
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
