package com.pmsconnect.mage;

import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.connector.ConnectorAsyncService;
import com.pmsconnect.mage.connector.ConnectorRepository;
import com.pmsconnect.mage.connector.ConnectorService;
import com.pmsconnect.mage.project.Project;
import com.pmsconnect.mage.project.ProjectRepository;
import com.pmsconnect.mage.project.ProjectService;
import com.pmsconnect.mage.project.coordination.ActivityState;
import com.pmsconnect.mage.project.coordination.CoordinationPair;
import com.pmsconnect.mage.user.Bridge;
import com.pmsconnect.mage.user.User;
import com.pmsconnect.mage.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MageApplicationTests {
	private Project projectA;

	@Autowired
	private ConnectorRepository connectorRepository;

	@Autowired
	private ConnectorService connectorService;

	@Autowired
	private ConnectorAsyncService connectorAsyncService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectService projectService;

	@BeforeAll
	public void cleanUp() {
		connectorRepository.deleteAll();
		projectRepository.deleteAll();
	}

	@BeforeEach
	public void createProject() {
		String projectId = projectService.addNewProject("BabyPooh");
		projectA = projectService.getProject(projectId);
		List<String> participant = Arrays.asList(new String[]{"sunny", "cherry", "teddy"});
		projectA.setParticipatingUserIds(participant);
		projectRepository.save(projectA);
	}

	@Test
	void addUser() {
		User userSunny = new User("sunny", "sunny", "actor");
		userRepository.save(userSunny);

		User userCherry = new User("cherry", "cherry", "actor");
		userRepository.save(userCherry);

		User userTeddy = new User("teddy", "teddy", "actor");
		userRepository.save(userTeddy);
	}

	@Test
	void addConnector() throws InterruptedException {
		// Connection declarations ----------------------------------
		Bridge bridgeP1 = new Bridge("gitlab", "sunny", "abcd", "gitlab.com/hello", "workspace/hello",
				"sunny", "abcd", "core-bape", "bape.fr/hello", "P1", "P1_inst");
		String connectorIdP1 = connectorService.addNewConnector("sunny", bridgeP1, false);
		Connector connectorP1 = connectorService.getConnector(connectorIdP1);
		connectorP1.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Sunny.xml"}));
		connectorRepository.save(connectorP1);

		Bridge bridgeP2 = new Bridge("gitlab", "cherry", "abcd", "gitlab.com/bonjour", "workspace/bonjour",
				"cherry", "abcd", "core-bape", "bape.fr/bonjour", "P2", "P2_inst");
		String connectorIdP2 = connectorService.addNewConnector("cherry", bridgeP2, false);
		Connector connectorP2 = connectorService.getConnector(connectorIdP2);
		connectorP2.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Cherry.xml"}));
		connectorRepository.save(connectorP2);

		Bridge bridgeP31 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
				"teddy", "abcd", "core-bape", "bape.fr/xin-chao", "P3", "P3_inst");
		Bridge bridgeP32 = new Bridge("github", "teddy", "abcd", "github.com/xin-chao", "workspace/xin-chao",
				"teddy", "abcd", "core-bape", "bape.fr/xin-chao", "P3", "P3_inst");
		String connectorIdP31 = connectorService.addNewConnector("teddy", bridgeP31, false);
		Connector connectorP31 = connectorService.getConnector(connectorIdP31);
		connectorP31.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
		connectorRepository.save(connectorP31);

		String connectorIdP32 = connectorService.addNewConnector("teddy", bridgeP32, false);
		Connector connectorP32 = connectorService.getConnector(connectorIdP32);
		connectorP32.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
		connectorRepository.save(connectorP32);

		// put all the connections into the project ----------------------------------
		Map<String, String> connectorBoards = new HashMap<>();
		connectorBoards.put(connectorP1.getId(), "P1");
		connectorBoards.put(connectorP2.getId(), "P2");
		connectorBoards.put(connectorP31.getId(), "P3");
		connectorBoards.put(connectorP32.getId(), "P3");
		projectA.setParticipateConnectionIds(connectorBoards);


		// set up the coordination pair ----------------------------------
		CoordinationPair pairP12 = new CoordinationPair("P1", "P2",
				"T2", ActivityState.FINISHED, "T4", ActivityState.STARTED);
		CoordinationPair pairP23 = new CoordinationPair("P2", "P3",
				"T5", ActivityState.FINISHED, "T6", ActivityState.FINISHED);
		CoordinationPair pairP13 = new CoordinationPair("P3", "P1",
				"T2", ActivityState.STARTED, "T7", ActivityState.STARTED);

		List<CoordinationPair> coordinationTables = new ArrayList<>();
		coordinationTables.add(pairP12);
		coordinationTables.add(pairP23);
		coordinationTables.add(pairP13);
		projectA.setCoordinationPoints(coordinationTables);
		projectRepository.save(projectA);

		// generate the action linkage ----------------------------------
		System.out.println(connectorService.generateActionLinkageTest(connectorP1));
		connectorService.generateActionLinkageTest(connectorP2);
		connectorService.generateActionLinkageTest(connectorP31);
		connectorService.generateActionLinkageTest(connectorP32);

		// start monitoring the jobs
//		connectorAsyncService.monitorProcessInstance(connectorIdP1);
//		connectorAsyncService.monitorProcessInstance(connectorIdP2);
//		connectorAsyncService.monitorProcessInstance(connectorIdP31);
//		connectorAsyncService.monitorProcessInstance(connectorIdP32);

//		Thread.sleep(5000);
//		connectorService.stopMonitoringProcessInstance(connectorIdP1);
//		connectorService.stopMonitoringProcessInstance(connectorIdP2);
//		connectorService.stopMonitoringProcessInstance(connectorIdP31);
//		connectorService.stopMonitoringProcessInstance(connectorIdP32);
	}
}
