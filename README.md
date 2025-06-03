## pMage

### About
pMage is an intermediate service who plays as an automatic integrator and reporter for updating user progression on their tasks to the corresponding process management system.
The integration is managed by a connector, which access user target application to be monitored and the target process instance in the process management system.

### How to use
#### Set up connection
By modifying the preset `MageApplicationTests.addConnector()`, you configure your project and add participating connections from your application and PMS.

#### Verify the connections
Using the specific API call implemented in `ConnectorController.getConnector`, you can check the entire information of the newly created connection.

#### Monitor the choreography
On each connected connector,
- Using the specific API call implemented in `ConnectorController.monitorProcessInstance`, you can start monitoring user behavior on the target application of the required connector.
- Using the specific API call implemented in `ConnectorController.stopMonitoringProcessInstance`, you can stop monitoring user behavior on the target application of the required connector.

Only connectors who is being monitored can participate in the process choreography