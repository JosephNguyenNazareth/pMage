package com.pmsconnect.mage.connector;

import com.pmsconnect.mage.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Dictionary;
import java.util.List;

@RestController
@RequestMapping(path = "api/mage")
public class ConnectorController {
    private final ConnectorService connectorService;
    private final ConnectorAsyncService connectorAsyncService;

    @Autowired
    public ConnectorController(ConnectorService connectorService, ConnectorAsyncService connectorAsyncService) {
        this.connectorService = connectorService;
        this.connectorAsyncService = connectorAsyncService;
    }

    @GetMapping
    public List<Connector> getConnectors() {
        return connectorService.getConnectors();
    }

    @GetMapping(path = "{connectorId}")
    public Connector getConnector(@PathVariable("connectorId") String connectorId) {
        return connectorService.getConnector(connectorId);
    }

    @PostMapping
    public String addNewConnector(
            @RequestParam String url,
            @RequestParam String pms,
            @RequestParam(required = false) String pmsProjectId,
            @RequestBody(required = false) UserRepo user) {
        return connectorService.addNewConnector(url, pms, pmsProjectId, user);
    }

    @PutMapping(path = "{connectorId}")
    public void updateConnector(
            @PathVariable("connectorId") String connectorId,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String pms,
            @RequestParam(required = false) String pmsProjectId,
            @RequestBody(required = false) UserRepo user) {
        connectorService.updateConnector(connectorId, url, pms, pmsProjectId, user);
    }

    @PutMapping(path = "{connectorId}/create-process")
    public String createProcessInstance(
            @PathVariable("connectorId") String connectorId,
            @RequestParam String processName) {
        return connectorService.createProcessInstance(connectorId, processName);
    }

    @GetMapping(path = "{connectorId}/get-process")
    public String getProcessInstance(
            @PathVariable("connectorId") String connectorId) {
        return connectorService.getProcessInstance(connectorId);
    }

    @GetMapping(path = "{connectorId}/monitor")
    public void monitorProcessInstance(
            @PathVariable("connectorId") String connectorId) {
        connectorAsyncService.monitorProcessInstance(connectorId);
    }

    @GetMapping(path = "{connectorId}/end-monitor")
    public void stopMonitoringProcessInstance(
            @PathVariable("connectorId") String connectorId) {
        connectorService.stopMonitoringProcessInstance(connectorId);
    }

    @GetMapping(path = "{connectorId}/end-task")
    public void endTaskInstance(
            @PathVariable("connectorId") String connectorId,
            @RequestParam String taskId,
            @RequestParam String commitMessage) {
        connectorAsyncService.endTaskInstance(connectorId, taskId,  commitMessage);
    }

    @GetMapping(path = "{connectorId}/all-commit")
    public List<Dictionary<String, String>> getLatestCommit(
            @PathVariable("connectorId") String connectorId) {
        return connectorAsyncService.getAllCommit(connectorId);
    }
}
