package com.ai.agent.response;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

@Description("Agent 结构化输出：包含对用户的回复、可选的日记正文，以及可选的图片 URL 列表（用于周总结情绪图或用户上传图片引用）")
public record AgentChatResponse(
        @Description("对用户的回复文本：包含下一步操作建议 + 积极/共情的情绪支持") String responseText,
        @Description("日记正文（可选）：当用户在记录/完善日记时返回；非日记场景可为空") String diaryText,
        @Description("图片 URL 列表（可选）：周总结情绪图或用户上传图片的可访问地址；无图片时为空/空列表") List<String> imgUrls
) {
}
