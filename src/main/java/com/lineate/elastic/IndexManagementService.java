package com.lineate.elastic;

import com.lineate.elastic.api.doc.ElasticDocApi;
import com.lineate.elastic.api.index.ElasticIndexApi;
import com.lineate.elastic.api.task.ElasticTaskApi;
import com.lineate.elastic.configuration.EntitySearchProperties;
import com.lineate.elastic.dto.StatusResponse;
import com.lineate.elastic.enums.DataIndexerTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IndexManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexManagementService.class);
    private final ConcurrentHashMap<DataIndexerTypes, String> elasticTasks = new ConcurrentHashMap<>();
    private final ElasticIndexApi indexApi;
    private final ElasticDocApi docApi;
    private final ElasticTaskApi taskApi;

    public IndexManagementService(ElasticIndexApi indexApi, ElasticDocApi docApi, ElasticTaskApi taskApi) {
        this.indexApi = indexApi;
        this.docApi = docApi;
        this.taskApi = taskApi;
    }

    public StatusResponse createIndex(EntitySearchProperties properties) {
        LOGGER.info("Creating index with name {} using config file {}",
                properties.getIndexName(), properties.getConfigFile());
        if (indexApi.checkIndexExists(properties.getIndexName())) {
            return new StatusResponse(StatusResponse.Status.ERROR, "Index already exists");
        }
        String newIndexName = properties.getIndexName() + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        if (indexApi.createIndex(newIndexName, properties.getConfigFile())
                && indexApi.addAliasToIndex(newIndexName, properties.getIndexName())) {
            return StatusResponse.OK;
        }
        return new StatusResponse(StatusResponse.Status.ERROR, "Could not create index");
    }

    public StatusResponse deleteIndex(EntitySearchProperties properties) {
        LOGGER.info("Deleting index {}", properties.getIndexName());

        String indexRealName = indexApi.getIndexNameByAlias(properties.getIndexName());
        if (indexRealName != null) {
            if (indexApi.deleteIndex(indexRealName)) {
                return StatusResponse.OK;
            }
        }
        return new StatusResponse(StatusResponse.Status.ERROR, "Could not delete index");
    }

    public StatusResponse reindex(EntitySearchProperties properties) {

        return StatusResponse.OK;
    }

    public StatusResponse cancelReindexing(EntitySearchProperties properties) {

        return StatusResponse.OK;
    }

    public StatusResponse getReindexingJobStatus(DataIndexerTypes indexerType) {

        return StatusResponse.OK;
    }
}
