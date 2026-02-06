package com.knowledge.agent.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_agent_configs")
public class UserAgentConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String agentName;
    private String personalityTags; // JSON
    private String interactionStyle;
}
