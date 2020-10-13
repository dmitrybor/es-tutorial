package com.lineate.elastic.controller;

import com.lineate.elastic.configuration.CompanySearchProperties;
import com.lineate.elastic.configuration.ContactSearchProperties;
import com.lineate.elastic.configuration.EntitySearchProperties;
import com.lineate.elastic.configuration.GroupSearchProperties;
import com.lineate.elastic.configuration.InternalUserSearchProperties;
import com.lineate.elastic.configuration.ProductSearchProperties;
import com.lineate.elastic.configuration.ProjectSearchProperties;
import com.lineate.elastic.configuration.RecordingSearchProperties;
import com.lineate.elastic.configuration.WorkSearchProperties;
import com.lineate.elastic.dto.StatusResponse;
import com.lineate.elastic.dto.TaskStatusResponse;
import com.lineate.elastic.enums.DataIndexerTypes;
import com.lineate.elastic.service.IndexManagementService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/index")
public class IndexManagementController {

    private final HashMap<DataIndexerTypes, EntitySearchProperties> indexerProperties = new HashMap<>();

    private final IndexManagementService indexManagementService;

    public IndexManagementController(IndexManagementService indexManagementService,
                                     ProductSearchProperties productSearchProperties,
                                     RecordingSearchProperties recordingSearchProperties,
                                     ContactSearchProperties contactSearchProperties,
                                     ProjectSearchProperties projectSearchProperties,
                                     InternalUserSearchProperties internalUserSearchProperties,
                                     GroupSearchProperties groupSearchProperties,
                                     WorkSearchProperties workSearchProperties,
                                     CompanySearchProperties companySearchProperties) {
        this.indexManagementService = indexManagementService;
        this.indexerProperties.put(DataIndexerTypes.PRODUCTS, productSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.RECORDINGS, recordingSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.CONTACTS, contactSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.PROJECTS, projectSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.INTERNAL_USERS, internalUserSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.GROUPS, groupSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.WORKS, workSearchProperties);
        this.indexerProperties.put(DataIndexerTypes.COMPANIES, companySearchProperties);

    }

    @PutMapping("/{indexerType}")
    public StatusResponse createIndex(@PathVariable("indexerType") DataIndexerTypes indexerType,
                                      @RequestParam(value = "numberOfShards", defaultValue = "1") int numberOfShards,
                                      @RequestParam(value = "numberOfReplicas", defaultValue = "0") int numberOfReplicas) {

        EntitySearchProperties properties = indexerProperties.get(indexerType);
        properties.setNumberOfShards(numberOfShards);
        properties.setNumberOfReplicas(numberOfReplicas);
        return indexManagementService.createIndex(properties);
    }

    @DeleteMapping("/{indexerType}")
    public StatusResponse deleteIndex(@PathVariable("indexerType") DataIndexerTypes indexerType) {
        EntitySearchProperties properties = indexerProperties.get(indexerType);
        return indexManagementService.deleteIndex(properties);
    }

    @PutMapping("/reindexing/{indexerType}")
    public StatusResponse reindex(@PathVariable("indexerType") DataIndexerTypes indexerType) {
        EntitySearchProperties properties = indexerProperties.get(indexerType);
        return indexManagementService.reindex(properties);
    }

    @GetMapping("/reindexing/{indexerType}")
    public TaskStatusResponse getReindexingJobStatus(@PathVariable("indexerType") DataIndexerTypes indexerType) {
        EntitySearchProperties properties = indexerProperties.get(indexerType);
        return indexManagementService.getReindexingTaskStatus(properties);
    }

    @DeleteMapping("/reindexing/{indexerType}")
    public StatusResponse cancelReindexing(@PathVariable("indexerType") DataIndexerTypes indexerType) {
        EntitySearchProperties properties = indexerProperties.get(indexerType);
        return indexManagementService.cancelReindexing(properties);
    }

}
