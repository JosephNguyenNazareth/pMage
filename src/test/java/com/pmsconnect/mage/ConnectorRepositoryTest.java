package com.pmsconnect.mage;

import com.pmsconnect.mage.connector.Connector;
import com.pmsconnect.mage.connector.ConnectorRepository;
import com.pmsconnect.mage.user.Bridge;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConnectorRepositoryTest {
    @Autowired
    private ConnectorRepository connectorRepository;

    @Test
    public void testAddConnector() {
        // Connection declarations ----------------------------------
        Bridge bridgeP1 = new Bridge("gitlab", "sunny", "abcd", "gitlab.com/hello", "workspace/hello",
                "sunny", "abcd", "core-bape", "bape.fr/hello", "P1", "P1_inst");
        Connector connectorP1 = new Connector(bridgeP1);
        connectorRepository.save(connectorP1);
    }
}