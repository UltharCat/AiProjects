import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    @Test
    void shouldContainRequiredRagDevSettings() {
        Properties properties = loadYaml("application-dev.yml");

        assertEquals("knowledge-agent-rag", properties.getProperty("spring.application.name"));
        assertTrue(properties.getProperty("spring.datasource.url").contains("knowledge_agent"));
        assertEquals("knowledge-rag", properties.getProperty("dubbo.application.name"));
        assertTrue(properties.getProperty("langchain4j.open-ai.embedding-model.dimensions").contains("MILVUS_DIMENSION"));
        assertEquals("${MILVUS_DIMENSION:1024}", properties.getProperty("milvus.cloud.dimension"));
        assertEquals("${DUBBO_CONFIG_CENTER_ADDRESS:N/A}", properties.getProperty("dubbo.config-center.address"));
    }

    private Properties loadYaml(String path) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource(path));
        Properties properties = factoryBean.getObject();
        assertNotNull(properties);
        return properties;
    }
}
