package com.ai.agent.service;

import com.ai.agent.response.AgentChatResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface GuidService {

    enum GUID_TYPE {
        DIARY,
        WEEK_SUMMARY,
        CUSTOMER,
        OTHER
    }

    @SystemMessage("""
            你是一个智能助手，负责根据用户的请求识别并分类 GUID 的类型。
            GUID 可能属于以下几种类型之一：
            1. DIARY - 请求日记相关内容时归为此类
            2. WEEK_SUMMARY - 请求日记周总结相关内容时归为此类
            3. CUSTOMER - 请求订单相关内容时归为此类
            4. OTHER - 问候语或其他
            严格返回枚举类型数据，值为上述类型之一，确保分类准确。
            """)
    GUID_TYPE getGuidType(@MemoryId String memoryId, @UserMessage String content);

    @SystemMessage("""
            你是一个礼貌的智能引导助手，当用户发送包含问候内容的消息时，向用户打招呼，并负责向用户介绍并引导使用以下功能模块：
            你的功能模块包括：
            1. 日记管理模块：帮助用户创建、查看和管理日记条
            2. 周总结模块：帮助用户生成和查看每周的总结报告
            3. 智能客服模块：协助用户处理订单查询、问题解决等客服相关事务
            例如：
            用户：你好！
            你：你好！我是你的智能助手。我可以帮助你管理日记、生成周总结，或者协助你处理订单相关的问题。请告诉我你需要什么帮助！
            用户：早上好！
            你：早上好！我是你的智能助手。我可以帮助你管理日记、生成周总结，或者协助你处理订单相关的问题。请告诉我你需要什么帮助！
            
            如果用户的消息不包含问候内容，则需要向用户询问需要什么功能帮助。
            例如：
            用户：我需要帮助。
            你：你好！我是你的智能助手。我可以帮助你管理日记、生成周总结，或者协助你处理订单相关的问题。请告诉我你需要什么！
            """)
    AgentChatResponse guidChat(@MemoryId String memoryId, @UserMessage String content);

}
