package com.ai.model.config.memory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "x-project.app.chat.memory")
@Data
public class ChatMemoryProperties {

    /**
     * 内存类型：IN_MEMORY - 内存存储，REDIS - Redis存储
     */
    public enum MemoryType { IN_MEMORY, REDIS }

    /**
     * 内存类型，默认IN_MEMORY
     */
    private MemoryType type = MemoryType.IN_MEMORY;

    /**
     * 命名空间前缀，Redis存储时使用
     */
    private String namespace = "chat:memory:";

    /**
     * 内存TTL，默认24小时
     */
    private Duration ttl = Duration.ofHours(24);

    /**
     * 最大消息数，默认20，超过裁剪最早的消息
     * <=0 表示不裁剪
     */
    private int maxMessages = 20;

}
