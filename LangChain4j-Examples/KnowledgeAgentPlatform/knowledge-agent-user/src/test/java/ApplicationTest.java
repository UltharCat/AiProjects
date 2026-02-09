import com.knowledge.agent.user.KnowledgeAgentUserApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KnowledgeAgentUserApplication.class)
public class ApplicationTest {

    @Value("${spring.datasource.url}")
    public String test;


    @Test
    public void contextLoads() {
        System.out.println(test);
    }

}
