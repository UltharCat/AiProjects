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
        assertEquals("${REDIS_HOST:localhost}", dev.getProperty("spring.data.redis.host"));
        assertEquals("${ROCKETMQ_NAME_SERVER:localhost:9876}", dev.getProperty("rocketmq.name-server"));
        assertEquals("knowledge-agent-gateway", dev.getProperty("dubbo.application.name"));
        assertEquals("false", dev.getProperty("dubbo.registry.check"));
        assertEquals("${DUBBO_CONFIG_CENTER_ADDRESS:N/A}", dev.getProperty("dubbo.config-center.address"));
        assertEquals("${KNOWLEDGE_AGENT_TOKEN_ISSUER:knowledge-agent-platform}", dev.getProperty("knowledge-agent.auth.token-issuer"));
        assertEquals("${KNOWLEDGE_AGENT_REVIEW_DEDUP_WINDOW:PT30M}", dev.getProperty("knowledge-agent.review.dedup-window"));
        assertEquals("${KNOWLEDGE_AGENT_REVIEW_SCHEDULER_FIXED_DELAY_MS:900000}", dev.getProperty("knowledge-agent.review.scheduler.fixed-delay-ms"));
        assertEquals("${KNOWLEDGE_AGENT_REVIEW_BATCH_TOPIC:knowledge-agent-review-batch}", dev.getProperty("knowledge-agent.messaging.review-batch-topic"));
    }

    private Properties loadYaml(String path) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource(path));
        Properties properties = factoryBean.getObject();
        assertNotNull(properties);
        return properties;
    }
}
