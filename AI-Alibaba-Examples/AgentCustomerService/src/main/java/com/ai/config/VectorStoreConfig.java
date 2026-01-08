package com.ai.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@AutoConfiguration(
        after = {DruidDatasourceConfig.class}
)
@ConditionalOnClass({PgVectorStore.class, DataSource.class, JdbcTemplate.class})
@EnableConfigurationProperties({PgVectorStoreProperties.class})
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnMissingBean
    public PgVectorStore vectorStore(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate,
                                     EmbeddingModel embeddingModel,
                                     PgVectorStoreProperties properties,
                                     ObjectProvider<ObservationRegistry> observationRegistry,
                                     ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
                                     BatchingStrategy batchingStrategy) {
        boolean initializeSchema = properties.isInitializeSchema();
        return ((PgVectorStore.PgVectorStoreBuilder)((PgVectorStore.PgVectorStoreBuilder)((PgVectorStore.PgVectorStoreBuilder)PgVectorStore.builder(jdbcTemplate, embeddingModel).schemaName(properties.getSchemaName()).idType(properties.getIdType()).vectorTableName(properties.getTableName()).vectorTableValidationsEnabled(properties.isSchemaValidation()).dimensions(properties.getDimensions()).distanceType(properties.getDistanceType()).removeExistingVectorStoreTable(properties.isRemoveExistingVectorStoreTable()).indexType(properties.getIndexType()).initializeSchema(initializeSchema).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))).customObservationConvention((VectorStoreObservationConvention)customObservationConvention.getIfAvailable(() -> null))).batchingStrategy(batchingStrategy)).maxDocumentBatchSize(properties.getMaxDocumentBatchSize()).build();
    }

}
