import com.knowledge.agent.rag.KnowledgeAgentRagApplication;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KnowledgeAgentRagApplication.class)
public class ApplicationTest {

    @Autowired
    public EmbeddingModel embeddingModel;

    @Test
    public void contextLoads() {
        System.out.println("embeddingModel test: " + embeddingModel.embed("测试" + Math.random()));
    }

}
