package com.pmsconnect.mage.project;

import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.project.coordination.*;
import com.pmsconnect.mage.user.Bridge;
import com.pmsconnect.mage.utils.lock.GraphForPMage;
import com.pmsconnect.mage.utils.lock.LockDetect;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {
    Project projectA = new Project("BabyPooh");

    @Before
    public void createProject() {
        List<String> participant = Arrays.asList(new String[]{"sunny", "cherry", "teddy"});
        projectA.setParticipatingUserIds(participant);

        List<String> connections = Arrays.asList(new String[]{"s11", "c11", "t11", "t21"});
        projectA.setParticipateConnectionIds(connections);
    }

    @Test
    public void addConnectionsMultipleProcesses() {
        Bridge bridgeP1 = new Bridge("gitlab", "sunny", "abcd", "gitlab.com/hello", "workspace/hello",
                                    "sunny", "abcd", "core_bape", "bape.fr/hello", "P1", "P1_inst");
        Connector connectorP1 = new Connector(bridgeP1);

        Bridge bridgeP2 = new Bridge("gitlab", "cherry", "abcd", "gitlab.com/bonjour", "workspace/bonjour",
                "cherry", "abcd", "core_bape", "bape.fr/bonjour", "P2", "P2_inst");
        Connector connectorP2 = new Connector(bridgeP2);

        Bridge bridgeP31 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
        Bridge bridgeP32 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
        Connector connectorP31 = new Connector(bridgeP31);
        Connector connectorP32 = new Connector(bridgeP32);

        List<String> connectorBoards = Arrays.asList(new String[]{connectorP1.getId(), connectorP2.getId(), connectorP31.getId(), connectorP32.getId()});
        projectA.setParticipateConnectionIds(connectorBoards);

        CoordinationPair pairP12 = new CoordinationPair("P1", "P2",
                "T2", ActivityState.FINISHED, "T3", ActivityState.STARTED);
        CoordinationPair pairP23 = new CoordinationPair("P2", "P3",
                "T5", ActivityState.FINISHED, "T6", ActivityState.FINISHED);
        CoordinationPair pairP13 = new CoordinationPair("P1", "P2",
                "T2", ActivityState.STARTED, "T7", ActivityState.STARTED);

        List<CoordinationPair> coordinationTables = Arrays.asList(new CoordinationPair[]{pairP12, pairP23, pairP13});
        projectA.setCoordinationPoints(coordinationTables);

        System.out.println(projectA);
    }

    @Test
    public void convertProjectBape2pMageGraph() {
        GraphConvert graphP1 = new GraphConvertFromBape();
        graphP1.convert(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Sunny.xml"}));
        System.out.println(Arrays.deepToString(graphP1.getGraph().asMatrix()));

        GraphConvert graphP2 = new GraphConvertFromBape();
        graphP2.convert(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Cherry.xml"}));
        System.out.println(Arrays.deepToString(graphP2.getGraph().asMatrix()));

        GraphConvert graphP3 = new GraphConvertFromBape();
        graphP3.convert(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
        System.out.println(Arrays.deepToString(graphP3.getGraph().asMatrix()));
    }

    @Test
    public void completeSetupAndDetectDeadlock() {
        Bridge bridgeP1 = new Bridge("gitlab", "sunny", "abcd", "gitlab.com/hello", "workspace/hello",
                "sunny", "abcd", "core_bape", "bape.fr/hello", "P1", "P1_inst");
        Connector connectorP1 = new Connector(bridgeP1);
        connectorP1.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Sunny.xml"}));

        Bridge bridgeP2 = new Bridge("gitlab", "cherry", "abcd", "gitlab.com/bonjour", "workspace/bonjour",
                "cherry", "abcd", "core_bape", "bape.fr/bonjour", "P2", "P2_inst");
        Connector connectorP2 = new Connector(bridgeP2);
        connectorP2.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Cherry.xml"}));

        Bridge bridgeP31 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
        Bridge bridgeP32 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
        Connector connectorP31 = new Connector(bridgeP31);
        connectorP31.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
        Connector connectorP32 = new Connector(bridgeP32);
        connectorP32.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));



        List<String> connectorBoards = Arrays.asList(new String[]{connectorP1.getId(), connectorP2.getId(), connectorP31.getId(), connectorP32.getId()});
        projectA.setParticipateConnectionIds(connectorBoards);

        CoordinationPair pairP12 = new CoordinationPair("P1", "P2",
                "T2", ActivityState.FINISHED, "T3", ActivityState.STARTED);
        CoordinationPair pairP23 = new CoordinationPair("P2", "P3",
                "T5", ActivityState.FINISHED, "T6", ActivityState.FINISHED);
        CoordinationPair pairP13 = new CoordinationPair("P3", "P1",
                "T6", ActivityState.STARTED, "T2", ActivityState.STARTED);

        List<CoordinationPair> coordinationTables = Arrays.asList(new CoordinationPair[]{pairP12, pairP23, pairP13});
        projectA.setCoordinationPoints(coordinationTables);


        // now convert the process design into graph
        GraphConvert graphP1 = new GraphConvertFromBape();
        graphP1.convert(connectorP1.getBridge().getProcessDesignPaths());

        GraphConvert graphP2 = new GraphConvertFromBape();
        graphP2.convert(connectorP2.getBridge().getProcessDesignPaths());

        GraphConvert graphP31 = new GraphConvertFromBape();
        graphP31.convert(connectorP31.getBridge().getProcessDesignPaths());

        // we want to examine if pMage can handle process model duplication
        GraphConvert graphP32 = new GraphConvertFromBape();
        graphP32.convert(connectorP32.getBridge().getProcessDesignPaths());

        // next convert the coordination pairs to graph
        GraphConvertFromCoordinationPair graphCoor = new GraphConvertFromCoordinationPair();
        graphCoor.convert(projectA.getCoordinationPoints());

        // combine all the fragments into the final graph
        GraphForPMage finalGraph = new GraphForPMage();
        finalGraph.combine(Arrays.asList(new GraphForPMage[] {graphP1.getGraph(), graphP2.getGraph(), graphP31.getGraph(), graphP32.getGraph(), graphCoor.getGraph()}));

        System.out.println(Arrays.deepToString(finalGraph.asMatrix()));

        // checking the possible deadlocks or livelocks - true answer is YES
        System.out.println(LockDetect.isCyclic(finalGraph.getAdjacencyList()));
    }
}