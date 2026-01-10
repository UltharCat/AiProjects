package com.ai.service.impl;

import com.ai.request.AgentChatRequest;
import com.ai.service.AgentService;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class AgentServiceImpl implements AgentService {

    private final ChatClient chatClient;

    public AgentServiceImpl(ChatClient.Builder builder,
                            RedissonRedisChatMemoryRepository redissonRedisChatMemoryRepository,
                            VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一个智能的电商客服助手，能够帮助用户解决购物过程中遇到的问题。
                        你可以提供订单查询、订单取消、商品推荐等操作。
                        你在帮助用户进行操作前，需要确认用户ID，并且仅提供该用户ID下的信息，额外信息不予提供。
                        在回答用户提问前，需要对用户的聊天记录进行分析，避免重复查询。
                        提供不同功能时需要注意：
                        1.订单查询时需要根据用户ID查询相应订单，并且在聊天记录中已经存在的订单信息不需要重复查询，返回聊天记录中的信息即可。
                        2.订单取消时，需要确认用户ID和订单ID，并让用户确认是否取消订单，只有在用户确认后才进行取消操作。
                        3.商品推荐时，可以根据用户的购买历史、聊天记录和浏览记录进行推荐，避免推荐用户已经购买过的商品。
                        4.如果用户的问题无法通过上述功能解决，请礼貌地告知用户，并建议用户联系人工客服。
                        5.回答中涉及用户敏感信息的内容需要进行脱敏处理，使用*进行敏感信息替代。
                        6.如果用户提供的信息不完整，请主动询问所需的补充信息。
                        7.请使用中文且简洁明了的语言进行回答，避免使用专业术语。
                        提供功能操作时需要使用提供的工具，且必须严格按照工具的输入格式进行调用，不能自行编造格式。
                        今日日期是{current_date}。
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        // 使用redis作为聊天记忆存储
                                        .chatMemoryRepository(redissonRedisChatMemoryRepository)
                                        // 100条聊天信息以内的历史记忆
                                        .maxMessages(100)
                                        .build()
                        ).build(),
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(
                                        VectorStoreDocumentRetriever.builder()
                                                .vectorStore(vectorStore)
                                                // 相似度阈值0.4，低于该阈值的文档将被过滤掉
                                                .similarityThreshold(0.4)
                                                // 每次检索返回最相似的10条文档
                                                .topK(10)
                                                .build()
                                )
                                // 可配置多个检索增强前置处理器，先rewrite补全提问，再compression消除歧义去噪，还可以用translation翻译成英文等，也可以再加上Expander进行扩展，按照先后顺序执行
//                                .queryTransformers(RewriteQueryTransformer.builder()
//                                                //  通过Mutate克隆了一个builder，避免多个builder使用冲突
//                                                .chatClientBuilder(builder.build().mutate())
//                                                .build(),
//                                        CompressionQueryTransformer.builder()
//                                                .chatClientBuilder(builder.build().mutate())
//                                                .build())
                                .build()
                )
                .defaultTools()
                .build();
    }

    @Override
    public Flux<String> chat(AgentChatRequest request) {
        return Flux.defer(() -> chatClient.prompt()
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .user(request.getContent())
                .advisors(a -> a.param(CONVERSATION_ID, request.getConversationId()))
                .stream()
                .chatResponse()
                .filter(Objects::nonNull)
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
        ).subscribeOn(Schedulers.boundedElastic());
    }

}
