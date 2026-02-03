/*
 * V1.0.0__init_vector_schema.sql
 * PostgreSQL Vector Data Schema
 * 包含：向量插件初始化、日记向量存储、情感知识库
 */

-- 1. 启用 pgvector 插件
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 长期记忆 - 日记向量表
-- 用于存储拆分后的日记片段，支持语义检索
CREATE TABLE IF NOT EXISTS diary_memory_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,          -- 关联 MySQL 的 user_id
    diary_entry_id BIGINT NOT NULL,   -- 关联 MySQL 的 diary_entries.id
    content_segment TEXT NOT NULL,    -- 文本片段
    embedding vector(1536),           -- 向量数据 (维度取决于模型，如 Qwen/OpenAI 使用 1536)
    metadata JSONB DEFAULT '{}'::jsonb, -- 额外元数据 (如日期、情感标签)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建 HNSW 索引以加速查询 (L2 距离)
CREATE INDEX ON diary_memory_store USING hnsw (embedding vector_l2_ops);
-- 创建用户索引，通常查询都会带 user_id 过滤
CREATE INDEX idx_memory_user_id ON diary_memory_store(user_id);


-- 3. RAG 知识库 - 情感分析专业库
-- 用于 Agent 决策时的参考知识
CREATE TABLE IF NOT EXISTS psychology_knowledge_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),            -- 分类：如 "焦虑调节", "正念", "沟通技巧"
    embedding vector(1536),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON psychology_knowledge_base USING hnsw (embedding vector_cosine_ops);
