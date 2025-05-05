package com.pmsconnect.mage.project.coordination;

import com.pmsconnect.mage.utils.lock.GraphForPMage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphConvertFromCoordinationPair {
    private final GraphForPMage graph ;

    public GraphConvertFromCoordinationPair() {
        this.graph = new GraphForPMage();
    }

    public void convert(Map<String, CoordinationPair> coordinationPairList) {
        for (CoordinationPair coordinationPair: coordinationPairList.values()) {
            graph.addEdge(coordinationPair.getPredecessorPoint(), coordinationPair.getSuccessorPoint());
        }
    }

    public GraphForPMage getGraph() {
        return graph;
    }
}
