import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;

public class KnowledgeAgentGatewayUnitTest {

    @Test
    public void generateEncryptedPassword() {
        // 1. 创建加密器
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        // 2. 配置加密参数 (这些通常与 application.yml 中的配置一致)
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("ultharKey"); // 设置加密秘钥 (jasypt.encryptor.password)
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setPoolSize("1");
        encryptor.setConfig(config);
        // 3. 执行加密
        String plainText = "nacos"; // 你想要加密的明文密码
        String encryptedText = encryptor.encrypt(plainText);
        System.out.println("Encrypted Password: " + encryptedText);
        System.out.println("Decrypted Password: " + encryptor.decrypt(encryptedText));
    }

}
