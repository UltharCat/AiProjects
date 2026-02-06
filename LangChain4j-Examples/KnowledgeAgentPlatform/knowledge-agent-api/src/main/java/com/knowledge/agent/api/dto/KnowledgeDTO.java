package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class KnowledgeDTO implements Serializable {

    /**
     * 知识内容
     */
    private String content;

    /**
     * 来源 (如：文件名、URL)
     */
    private String source;

    /**
     * 所有者ID
     */
    private Long ownerId;
}
