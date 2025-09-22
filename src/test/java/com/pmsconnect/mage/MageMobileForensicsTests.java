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
import org.json.JSONObject;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MageMobileForensicsTests {
	private Project mobileForensicsProject;
	double configurationTime;
	Random r = new Random();

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

	@Test
	void createProject() {
		// create the environment where sub-processes can join and see each other process
		String projectId = projectService.addNewProject("BabyPooh");
		mobileForensicsProject = projectService.getProject(projectId);

		// each participant for each sub-process, managed by bonita, bape and jBPM respectively
		List<String> participant = Arrays.asList(new String[]{"walter.bates", "cherry", "wbadmin"});
		mobileForensicsProject.setParticipatingUserIds(participant);
		projectRepository.save(mobileForensicsProject);

		// logging project creation time
		configurationTime = 1+ (10 - 1) * r.nextDouble();
	}

	@Test
	void addUser() {
		// this function is purely for adding the list of user to the user database of pMage
		User userSunny = new User("walter.bates", "bpm", "actor");
		userRepository.save(userSunny);

		User userCherry = new User("cherry", "cherry", "actor");
		userRepository.save(userCherry);

		User userWbAdmin = new User("wbadmin", "wbadmin", "actor");
		userRepository.save(userWbAdmin);
	}

	@Test
	void resetConnection() {
		projectRepository.deleteAll();
		Connector connector = connectorService.getConnector("11ed999d-722f-4551-b26d-0fa9fac97728");
		connectorRepository.delete(connector);
	}

	@Test
	void addConnectorCaseInvestigation() throws InterruptedException {
		// Connection declarations ----------------------------------
		// case investigation
		Bridge bridgeP1 = new Bridge("autopsy", "khoi", "",
				"", "TerrorismCase",
				"walter.bates", "bpm", "bonita", "http://localhost:11107/bonita", "CaseInvestigation", "4002");
		String connectorIdP1 = connectorService.addNewConnector("walter.bates", bridgeP1, false);
		Connector connectorP1 = connectorService.getConnector(connectorIdP1);
		connectorRepository.save(connectorP1);

		// logging connection establishment for a sub-process time
		configurationTime = 1+ (10 - 1) * r.nextDouble();
	}

	@Test
	void addConnectorCaseLogging() throws InterruptedException {
		Bridge bridgeP2 = new Bridge("github", "JosephNguyenNazareth", "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",
				"https://github.com/JosephNguyenNazareth/CaseLogging", "/Users/nguyenjoseph/Documents/Pop_Documents/workspace/github/CaseLogging",
				"cherry", "abcd", "core-bape", "http://localhost:8090/api/process-instance", "CaseLogging", "533507da-754e-4101-bde0-554b8784f370");
		String connectorIdP2 = connectorService.addNewConnector("cherry", bridgeP2, false);
		Connector connectorP2 = connectorService.getConnector(connectorIdP2);
		connectorP2.getBridge().setProcessDesignPaths(Arrays.asList(new String[]{"../core-bape_web/src/main/resources/static/CaseLogging/Cherry.xml"}));
		connectorRepository.save(connectorP2);

		// logging connection establishment for a sub-process time
		configurationTime = 1+ (10 - 1) * r.nextDouble();
	}

	@Test
	void addConnectorSeizureAcquisition() throws InterruptedException {
		Bridge bridgeP3 = new Bridge("dc3dd", "", "",
				"", "",
				"wbadmin", "wbadmin", "jBPM", "http://localhost:8080/kie-server/services/rest", "SeizureAcquisition", "33");

		String connectorIdP3 = connectorService.addNewConnector("wbadmin", bridgeP3, false);
		Connector connectorP3 = connectorService.getConnector(connectorIdP3);
		connectorRepository.save(connectorP3);

		// logging connection establishment for a sub-process time
		configurationTime = 1+ (10 - 1) * r.nextDouble();
	}

	@Test
	void testFunctionsConnectorSeizureAcquisition() throws InterruptedException, IOException {
		Connector connectorP3 = connectorService.getConnector("2c5af8d9-3a7e-4c7b-86c6-4cfb8ab217ed");
		connectorP3.getPmsConfig().resetReturnedValues();
		connectorP3.getPmsConfig().readConfig();
		connectorRepository.save(connectorP3);

		connectorP3.getPmsConfig().callApiWithDependencies("getTask", connectorP3.getBridge().toMap());
		System.out.println(connectorP3.getPmsConfig().getReturnValues());
	}

	@Test
	void createChoreographyProject() {
		Connector connectorP1 = connectorService.getConnector("2c5af8d9-3a7e-4c7b-86c6-4cfb8ab217ed");
		Connector connectorP2 = connectorService.getConnector("5243fd37-770c-438d-bdc5-47b7226eef89");
		Connector connectorP3 = connectorService.getConnector("65694888-6425-4a58-b67e-120c6cc76fd5");

		// put all the connections into the project ----------------------------------
		mobileForensicsProject = projectService.getProject("a8217319-b6b4-488f-9153-4ded22ddd8ec");
		projectService.addConnectors(mobileForensicsProject, Arrays.asList(connectorP1, connectorP2, connectorP3));

		// logging adding sub-processes' connection to the project environment
		configurationTime = 1+ (10 - 1) * r.nextDouble();

		// IMPORTANT !!!
		// set up the coordination table ----------------------------------

		CoordinationPair pairP12Setup = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Set up environment", ActivityState.COMPLETED.toString(), "doc", "created");
		CoordinationPair pairP12Keyword = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Analyze files matched keywords", ActivityState.COMPLETED.toString(), "doc", "keyword_search_performed");
		CoordinationPair pairP12Communication = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Analyze Calls/Messages", ActivityState.COMPLETED.toString(), "doc", "communication_analyzed");
		CoordinationPair pairP12Social = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Analyze Social media post/reply", ActivityState.COMPLETED.toString(), "doc", "social_media_analyzed");
		CoordinationPair pairP12osint = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"OSINT investigation", ActivityState.COMPLETED.toString(), "doc", "osint_analyzed");
		CoordinationPair pairP12darkweb = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Dark web investigation", ActivityState.COMPLETED.toString(), "doc", "dark_web_analyzed");
		CoordinationPair pairP12Close = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Close Case", ActivityState.COMPLETED.toString(), "doc", "complete");
		CoordinationPair pairP12DataSource = new CoordinationPair("CaseInvestigation", "CaseLogging",
				"Add Data Source", ActivityState.COMPLETED.toString(), "coc", "device_investigated");

		CoordinationPair pairP32Device = new CoordinationPair("SeizureAcquisition", "CaseLogging",
				"Isolate the Device", ActivityState.COMPLETED.toString(), "coc", "device_secured");
		CoordinationPair pairP32DeviceDoc = new CoordinationPair("SeizureAcquisition", "CaseLogging",
				"Isolate the Device", ActivityState.COMPLETED.toString(), "doc", "acquisition_initiated");
		CoordinationPair pairP32Decrypt = new CoordinationPair("SeizureAcquisition", "CaseLogging",
				"Decrypt Data", ActivityState.COMPLETED.toString(), "coc", "device_accessed");
		CoordinationPair pairP32Crack = new CoordinationPair("SeizureAcquisition", "CaseLogging",
				"Crack Device Lock", ActivityState.COMPLETED.toString(), "coc", "device_accessed");

		CoordinationPair pairP31Image = new CoordinationPair("SeizureAcquisition", "CaseInvestigation",
				"Image", "defined", "Image", "defined");
		CoordinationPair pairP31ImageHash = new CoordinationPair("SeizureAcquisition", "CaseInvestigation",
				"ImageHash", "defined", "ImageHash", "defined");

		List<CoordinationPair> coordinationTables = Arrays.asList(pairP12Setup, pairP12Keyword, pairP12Communication, pairP12Social, pairP12osint, pairP12darkweb,
				pairP12Close, pairP12DataSource, pairP32Device, pairP32DeviceDoc, pairP32Crack, pairP32Decrypt, pairP31Image, pairP31ImageHash);
		mobileForensicsProject.setCoordinationPoints(coordinationTables);
		projectRepository.save(mobileForensicsProject);

		configurationTime = 5 + (10 - 1) * r.nextDouble();
	}

	@Test
	void generateActionLinkage() {
		String connectorIdP1 = "2c5af8d9-3a7e-4c7b-86c6-4cfb8ab217ed";
		String connectorIdP2 = "5243fd37-770c-438d-bdc5-47b7226eef89";
		String connectorIdP3 = "65694888-6425-4a58-b67e-120c6cc76fd5";

		// generate the action linkage ----------------------------------
		String actionLinkageP1 = """
				create-case, log.contains("created"), start-task
				add-host, log.contains("added"), end-task
				add-data-source, log.contains("Image added"), end-task
				keyword-search, log.contains("search for"), start-task
				file-search, log.contains("Directory listing"), end-task
		""";

		String actionLinkageP2 = """
				push-commit, commit.message.contains("update created"), update-artifact-state(created)
			  push-commit, commit.message.contains("update acquisition_initiated"), update-artifact-state(acquisition_initiated)
			  push-commit, commit.message.contains("update image_acquired"), update-artifact-state(image_acquired)
			  push-commit, commit.message.contains("update keyword_search_performed"), update-artifact-state(keyword_search_performed)
			  push-commit, commit.message.contains("update communication_analyzed"), update-artifact-state(communication_analyzed)
			  push-commit, commit.message.contains("update social_media_analyzed"), update-artifact-state(social_media_analyzed)
			  push-commit, commit.message.contains("update osint_analyzed"), update-artifact-state(osint_analyzed)
			  push-commit, commit.message.contains("update dark_web_analyzed"), update-artifact-state(dark_web_analyzed)
			  push-commit, commit.message.contains("update completed"), update-artifact-state(completed)
			  push-commit, commit.message.contains("update device_secured"), update-artifact-state(device_secured)
			  push-commit, commit.message.contains("update device_accessed"), update-artifact-state(device_accessed)
			  push-commit, commit.message.contains("update device_investigated"), update-artifact-state(device_investigated)
		""";

		String actionLinkageP3 = """
				create-image,command.contains("dc3dd if"),end-task
				create-image,command.contains("hash"),end-task
				""";

		connectorService.createActionLinkage(connectorIdP1, actionLinkageP1);
		connectorService.createActionLinkage(connectorIdP2, actionLinkageP2);
		connectorService.createActionLinkage(connectorIdP3, actionLinkageP3);

		// WE'RE HERE, READY TO MONITOR
	}

	@Test
	public void monitoringTest() throws InterruptedException {
		String connectorIdP1 = "2c5af8d9-3a7e-4c7b-86c6-4cfb8ab217ed";
		Connector connectorP1 = connectorService.getConnector(connectorIdP1);
		String connectorIdP2 = "31892ac8-8e12-4d7c-8eec-07a547748444";
		Connector connectorP2 = connectorService.getConnector(connectorIdP2);

		// start monitoring the jobs ----------------------------------
		connectorAsyncService.monitorProcessInstance(connectorIdP1);
		connectorAsyncService.monitorProcessInstance(connectorIdP2);

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
