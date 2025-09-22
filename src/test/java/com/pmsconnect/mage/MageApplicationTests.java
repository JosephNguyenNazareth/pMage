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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

	@BeforeEach
	void createProject() {
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
		connectorRepository.deleteAll();
		projectRepository.deleteAll();

		// Connection declarations ----------------------------------
		Bridge bridgeP1 = new Bridge("chasca", "sunny", "abcd",
				"", "/Users/nguyenjoseph/Documents/Pop_Documents/workspace/chasca/hello",
				"sunny", "abcd", "core-bape", "http://localhost:8090/api/process-instance", "P1", "9f9f28d8-9e91-4d55-b75b-a94831d8fd37");
		String connectorIdP1 = connectorService.addNewConnector("sunny", bridgeP1, false);
		Connector connectorP1 = connectorService.getConnector(connectorIdP1);
		connectorP1.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Sunny.xml"}));
		connectorRepository.save(connectorP1);

		Bridge bridgeP2 = new Bridge("chasca", "cherry", "abcd",
				"", "/Users/nguyenjoseph/Documents/Pop_Documents/workspace/chasca/bonjour",
				"cherry", "abcd", "core-bape", "http://localhost:8090/api/process-instance", "P2", "a7d0964d-eb03-425a-b186-c7e0b34b960f");
		String connectorIdP2 = connectorService.addNewConnector("cherry", bridgeP2, false);
		Connector connectorP2 = connectorService.getConnector(connectorIdP2);
		connectorP2.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Cherry.xml"}));
		connectorRepository.save(connectorP2);

		Bridge bridgeP31 = new Bridge("chasca", "teddy", "abcd",
				"", "/Users/nguyenjoseph/Documents/Pop_Documents/workspace/chasca/xin-chao",
				"teddy", "abcd", "core-bape", "http://localhost:8090/api/process-instance", "P3", "11792fae-02e1-40a7-93fe-593e1803633b");
		Bridge bridgeP32 = new Bridge("chasca", "teddy", "abcd",
				"", "/Users/nguyenjoseph/Documents/Pop_Documents/workspace/chasca/xin-chao",
				"teddy", "abcd", "core-bape", "http://localhost:8090/api/process-instance", "P3", "d085ae7a-0d75-4683-b341-b51193eab251");
		String connectorIdP31 = connectorService.addNewConnector("teddy", bridgeP31, false);
		Connector connectorP31 = connectorService.getConnector(connectorIdP31);
		connectorP31.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
		connectorRepository.save(connectorP31);

		String connectorIdP32 = connectorService.addNewConnector("teddy", bridgeP32, false);
		Connector connectorP32 = connectorService.getConnector(connectorIdP32);
		connectorP32.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/Monitoring/Teddy.xml"}));
		connectorRepository.save(connectorP32);

		// put all the connections into the project ----------------------------------
		projectService.addConnectors(projectA, Arrays.asList(new Connector[]{connectorP1, connectorP2, connectorP31, connectorP32}));

		// set up the coordination pair ----------------------------------
		CoordinationPair pairP12 = new CoordinationPair("P1", "P2",
				"T2", ActivityState.COMPLETED.toString(), "T4", ActivityState.READY.toString());
		CoordinationPair pairP23 = new CoordinationPair("P2", "P3",
				"T5", ActivityState.COMPLETED.toString(), "T6", ActivityState.COMPLETED.toString());
		CoordinationPair pairP13 = new CoordinationPair("P3", "P1",
				"T2", ActivityState.READY.toString(), "T7", ActivityState.READY.toString());

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


	}

	@Test
	public void monitoringTest() throws InterruptedException {
		String connectorIdP1 = "60123635-5da1-4bfb-a126-72374141a726";
		Connector connectorP1 = connectorService.getConnector(connectorIdP1);
		String connectorIdP2 = "c36d06ac-8983-40a8-9c8b-1d6842f3d15c";
		Connector connectorP2 = connectorService.getConnector(connectorIdP2);

		// start monitoring the jobs ----------------------------------
		connectorAsyncService.monitorProcessInstance(connectorIdP1);
//		connectorAsyncService.monitorProcessInstance(connectorIdP2);
//		connectorAsyncService.monitorProcessInstance(connectorIdP31);
//		connectorAsyncService.monitorProcessInstance(connectorIdP32);

		Thread.sleep(5000);
		// simulation of the choreography among processes ----------------------------------
		// process P1
		Path filePathP1 = Paths.get(connectorP1.getBridge().getProjectDir() + "/chasca_log.txt");
		// T1
		List<String> linesT1Started = Arrays.asList("First line", "[2024-05-08T10:15:30] INFO [Hunt] Starting hunt for 'startTask T1'.", "Third line");

		try {
			Files.write(filePathP1, linesT1Started, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> linesT1Finished = Arrays.asList("First line", "[2024-05-08T12:15:30] INFO [Hunt] Starting hunt for 'endTask T1'.", "Third line");

		try {
			Files.write(filePathP1, linesT1Finished, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// T2
		List<String> linesT2Started = Arrays.asList("First line", "[2024-05-08T10:15:30] INFO [Hunt] Starting hunt for 'startTask T2'.", "Third line");

		try {
			Files.write(filePathP1, linesT2Started, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread.sleep(10000);

		// process P2
		Path filePathP2 = Paths.get(connectorP2.getBridge().getProjectDir() + "/chasca_log.txt");
		// T3
		List<String> linesT3Started = Arrays.asList("First line", "[2024-05-08T10:15:30] INFO [Hunt] Starting hunt for 'startTask T3'.", "Third line");

		try {
			Files.write(filePathP2, linesT3Started, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> linesT3Finished = Arrays.asList("First line", "[2024-05-08T12:15:30] INFO [Hunt] Starting hunt for 'endTask T3'.", "Third line");

		try {
			Files.write(filePathP2, linesT3Finished, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// T4
		List<String> linesT4Started = Arrays.asList("First line", "[2024-05-08T10:15:30] INFO [Hunt] Starting hunt for 'startTask T4'.", "Third line");

		try {
			Files.write(filePathP2, linesT4Started, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			System.out.println("File written successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// -> this task is expected to not be able to start


		// process P3
	}
}
