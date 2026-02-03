package com.ai.tools;

import apache.rocketmq.v2.Message;
import dev.langchain4j.agent.tool.Tool;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Component
public class DiaryTools {

    private final RocketMQTemplate rocketMQTemplate;

    public DiaryTools(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Tool(name = "send_diary", value = "将最终的日记或周日记总结发送给业务端进行持久记录，返回发送是否成功，成功返回true，失败返回false")
    public boolean sendDiary() {
        rocketMQTemplate.sendMessageInTransaction("diary_topic", new Message());
        return false;
    }

}
