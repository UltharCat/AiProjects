package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 结构化用户画像对象，供 User、Gateway、Agent 模块共享。
 */
@Data
@Builder
public class UserProfileDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 学习风格标签。
     */
    private String learningStyle;

    /**
     * 用户偏好的 JSON 字符串形式。
     */
    private String preferencesJson;

    /**
     * 用户偏好的结构化键值对。
     */
    private Map<String, String> preferences;
}
