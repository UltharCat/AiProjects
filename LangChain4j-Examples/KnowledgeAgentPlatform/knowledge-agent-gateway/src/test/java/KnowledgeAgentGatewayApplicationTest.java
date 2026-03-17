import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KnowledgeAgentGatewayApplicationTest {

    @Test
    void shouldContainRequiredGatewaySettings() {
        Properties base = loadYaml("application.yml");
        Properties dev = loadYaml("application-dev.yml");

        assertEquals("${SPRING_PROFILES_ACTIVE:dev}", base.getProperty("spring.profiles.active"));
        assertEquals("knowledge-agent-gateway", dev.getProperty("spring.application.name"));
        assertEquals("knowledge-agent-gateway", dev.getProperty("dubbo.application.name"));
        assertEquals("false", dev.getProperty("dubbo.registry.check"));
        assertEquals("${DUBBO_CONFIG_CENTER_ADDRESS:N/A}", dev.getProperty("dubbo.config-center.address"));
    }

    private Properties loadYaml(String path) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource(path));
        Properties properties = factoryBean.getObject();
        assertNotNull(properties);
        return properties;
    }
}
