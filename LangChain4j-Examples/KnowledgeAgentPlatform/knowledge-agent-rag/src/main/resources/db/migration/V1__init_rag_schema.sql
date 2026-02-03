-- Enable Vector Extension (Needs Superuser, usually done manually, but good to have)
CREATE EXTENSION IF NOT EXISTS vector;

-- LangChain4j Standard Embedding Store Table
CREATE TABLE IF NOT EXISTS embeddings (
    embedding_id UUID PRIMARY KEY,
    embedding VECTOR(1536), -- 1536 for OpenAI/DashScope compatible models, adjust if needed
    text TEXT,
    metadata JSONB
);

-- Index for IVFFlat (Approximate Nearest Neighbor)
-- Note: Requires some data to be effective, creating placeholder
-- CREATE INDEX ON embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
-- OR HNSW (Better performance usually)
-- CREATE INDEX ON embeddings USING hnsw (embedding vector_cosine_ops);

-- User Knowledge Metadata Table (Optional, for linking back to SQL/Neo4j)
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    embedding_id UUID REFERENCES embeddings(embedding_id),
    user_id BIGINT NOT NULL,
    concept_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
