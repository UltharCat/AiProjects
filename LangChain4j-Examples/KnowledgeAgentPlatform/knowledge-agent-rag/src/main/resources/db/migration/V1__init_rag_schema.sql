-- 启用向量扩展 (通常需要超级用户权限手动开启，但也保留在此处以防万一)
CREATE EXTENSION IF NOT EXISTS vector;

-- LangChain4j 标准嵌入存储表
CREATE TABLE IF NOT EXISTS embeddings (
    embedding_id UUID PRIMARY KEY,
    embedding VECTOR(1536), -- 1536 维向量，兼容 OpenAI/DashScope 模型，如有需要可调整
    text TEXT,
    metadata JSONB
);
-- 添加注释
COMMENT ON TABLE embeddings IS '向量嵌入存储表';
COMMENT ON COLUMN embeddings.embedding IS 'OpenAI/DashScope 兼容的1536维向量数据';
COMMENT ON COLUMN embeddings.text IS '原始文本内容';
COMMENT ON COLUMN embeddings.metadata IS '结构化元数据(JSON格式)';

-- IVFFlat 索引 (近似最近邻)
-- 注意：需要一些数据才能生效，创建占位符
-- CREATE INDEX ON embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
-- 或 HNSW (通常性能更好)
-- CREATE INDEX ON embeddings USING hnsw (embedding vector_cosine_ops);

-- 用户知识元数据表 (可选，用于关联 SQL/Neo4j)
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    embedding_id UUID REFERENCES embeddings(embedding_id),
    user_id BIGINT NOT NULL,
    concept_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE knowledge_chunks IS '知识片段业务元数据表';
COMMENT ON COLUMN knowledge_chunks.user_id IS '上传用户的ID';
COMMENT ON COLUMN knowledge_chunks.concept_id IS '关联的概念ID或图谱节点ID';
