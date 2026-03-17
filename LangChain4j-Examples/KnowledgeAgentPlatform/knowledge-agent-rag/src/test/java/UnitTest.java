import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UnitTest {

    @Test
    public void contextLoads() {
        assertDoesNotThrow(() -> System.getenv("GEMINI_API_KEY"));
    }
}
