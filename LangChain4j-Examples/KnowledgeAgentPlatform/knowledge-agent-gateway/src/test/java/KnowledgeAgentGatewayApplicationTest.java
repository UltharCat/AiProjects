import com.alibaba.cloud.nacos.annotation.NacosConfig;
import com.knowledge.agent.gateway.KnowledgeAgentGatewayApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KnowledgeAgentGatewayApplication.class)
public class KnowledgeAgentGatewayApplicationTest {

//    @NacosConfig(dataId = "agent_gateway", group = "KNOWLEDGE_AGENT", key = "server.port")
    @Value("${server.port}")
    private String serverPort;

    @Test
    public void contextLoads() {
        System.out.println("Server Port: " + serverPort);
    }

}
