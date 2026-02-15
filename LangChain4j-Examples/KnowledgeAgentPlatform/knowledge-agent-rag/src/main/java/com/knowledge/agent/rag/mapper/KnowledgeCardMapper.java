package com.knowledge.agent.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.agent.rag.entity.KnowledgeCard;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeCardMapper extends BaseMapper<KnowledgeCard> {
}
