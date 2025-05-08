package com.pmsconnect.mage.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.connector.ConnectorAsyncService;
import com.pmsconnect.mage.connector.ConnectorRepository;
import com.pmsconnect.mage.connector.ConnectorService;
import com.pmsconnect.mage.project.coordination.*;
import com.pmsconnect.mage.user.Bridge;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataMongoTest
public class ProjectChoreographyTest {
    private Project projectA;

    @Autowired
    private ConnectorRepository  connectorRepository;

//    @BeforeAll
//    public void cleanUp() {
//        connectorRepository.deleteAll();
//        projectRepository.deleteAll();// or deleteAllInBatch() if supported
//    }

//    @Before
//    public void createProject() {
//        String projectId = projectService.addNewProject("BabyPooh");
//        projectA = projectService.getProject(projectId);
//        List<String> participant = Arrays.asList(new String[]{"sunny", "cherry", "teddy"});
//        projectA.setParticipatingUserIds(participant);
//        projectRepository.save(projectA);
//    }

    @Test
    public void testChoreography() {
        // Connection declarations ----------------------------------
        Bridge bridgeP1 = new Bridge("gitlab", "sunny", "abcd", "gitlab.com/hello", "workspace/hello",
                "sunny", "abcd", "core-bape", "bape.fr/hello", "P1", "P1_inst");
        Connector connectorP1 = new Connector(bridgeP1);
        connectorRepository.save(connectorP1);
//        String connectorIdP1 = connectorService.addNewConnector(bridgeP1, false);
//        Connector connectorP1 = connectorService.getConnector(connectorIdP1);
//        connectorP1.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Sunny.xml"}));
//        connectorRepository.save(connectorP1);
//
//        Bridge bridgeP2 = new Bridge("gitlab", "cherry", "abcd", "gitlab.com/bonjour", "workspace/bonjour",
//                "cherry", "abcd", "core_bape", "bape.fr/bonjour", "P2", "P2_inst");
//        String connectorIdP2 = connectorService.addNewConnector(bridgeP2, false);
//        Connector connectorP2 = connectorService.getConnector(connectorIdP2);
//        connectorP2.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Cherry.xml"}));
//        connectorRepository.save(connectorP2);
//
//        Bridge bridgeP31 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
//                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
//        Bridge bridgeP32 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
//                "teddy", "abcd", "core_bape", "bape.fr/xin-chao", "P3", "P3_inst");
//        String connectorIdP31 = connectorService.addNewConnector(bridgeP31, false);
//        Connector connectorP31 = connectorService.getConnector(connectorIdP31);
//        connectorP31.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
//        connectorRepository.save(connectorP31);
//
//        String connectorIdP32 = connectorService.addNewConnector(bridgeP32, false);
//        Connector connectorP32 = connectorService.getConnector(connectorIdP32);
//        connectorP32.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core_bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
//        connectorRepository.save(connectorP32);
//
//
//        // put all the connections into the project ----------------------------------
//        Map<String, String> connectorBoards = new HashMap<>();
//        connectorBoards.put(connectorP1.getId(), "P1");
//        connectorBoards.put(connectorP2.getId(), "P2");
//        connectorBoards.put(connectorP31.getId(), "P3");
//        connectorBoards.put(connectorP32.getId(), "P3");
//        projectA.setParticipateConnectionIds(connectorBoards);
//
//
//        // set up the coordination pair ----------------------------------
//        CoordinationPair pairP12 = new CoordinationPair("P1", "P2",
//                "T2", ActivityState.FINISHED, "T4", ActivityState.STARTED);
//        CoordinationPair pairP23 = new CoordinationPair("P2", "P3",
//                "T5", ActivityState.FINISHED, "T6", ActivityState.FINISHED);
//        CoordinationPair pairP13 = new CoordinationPair("P3", "P1",
//                "T2", ActivityState.STARTED, "T7", ActivityState.STARTED);
//
//        List<CoordinationPair> coordinationTables = new ArrayList<>();
//        coordinationTables.add(pairP12);
//        coordinationTables.add(pairP23);
//        coordinationTables.add(pairP13);
//        projectA.setCoordinationPoints(coordinationTables);
//        projectRepository.save(projectA);
    }
}