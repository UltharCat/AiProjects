import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class KnowledgeAgentGatewayUnitTest {

    @Test
    public void generateEncryptedPassword() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("ultharKey");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setPoolSize("1");
        encryptor.setConfig(config);

        String plainText = "nacos";
        String encryptedText = encryptor.encrypt(plainText);

        assertNotEquals(plainText, encryptedText);
        assertEquals(plainText, encryptor.decrypt(encryptedText));
    }
}
