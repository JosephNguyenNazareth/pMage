package com.pmsconnect.mage.project.coordination;

import com.pmsconnect.mage.utils.lock.GraphForPMage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphConvertFromBape implements GraphConvert {
    private final GraphForPMage graph ;
    private final Map<String, String> artifactProducers;
    private final List<String> taskNames;

    public GraphConvertFromBape() {
        this.graph = new GraphForPMage();
        this.artifactProducers = new HashMap<>();
        this.taskNames = new ArrayList<>();
    }

    public void convert(List<String> processFragments) {
        for (String xmlFile : processFragments) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                doc.getDocumentElement().normalize();

                NodeList tasks = doc.getElementsByTagName("Task");
                List<String> fragmentTask = new ArrayList<>();
                for (int i = 0; i < tasks.getLength(); i++) {
                    Element task = (Element) tasks.item(i);
                    String taskName = task.getAttribute("name");
                    if (!fragmentTask.contains(taskName))
                        fragmentTask.add(taskName);
                    if (!this.taskNames.contains(taskName))
                        this.taskNames.add(taskName);

                    // Map output artifacts to producing tasks
                    NodeList outputs = task.getElementsByTagName("OutputArtifact");
                    for (int j = 0; j < outputs.getLength(); j++) {
                        Element output = (Element) outputs.item(j);
                        String[] artifactNames = output.getAttribute("name").split(",");
                        for (String name : artifactNames) {
                            artifactProducers.put(name.trim(), taskName);
                        }
                    }
                }

                // Now loop again to build edges based on input artifacts
                for (int i = 0; i < tasks.getLength(); i++) {
                    Element task = (Element) tasks.item(i);
                    String taskName = task.getAttribute("name");

                    NodeList inputs = task.getElementsByTagName("InputArtifact");
                    for (int j = 0; j < inputs.getLength(); j++) {
                        Element input = (Element) inputs.item(j);
                        String[] artifactNames = input.getAttribute("name").split(",");
                        for (String name : artifactNames) {
                            String trimmedName = name.trim();
                            if (artifactProducers.containsKey(trimmedName)) {
                                String producer = artifactProducers.get(trimmedName);
                                graph.addEdge(producer, taskName);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GraphForPMage getGraph() {
        return graph;
    }
}
