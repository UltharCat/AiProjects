import com.knowledge.agent.rag.KnowledgeAgentRagApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KnowledgeAgentRagApplication.class)
public class ApplicationTest {

    @Value("${spring.datasource.url}")
    public String test;


    @Test
    public void contextLoads() {
        System.out.println(test);
    }

}
