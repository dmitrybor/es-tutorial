package com.lineate.elastic.model;

public class TrackedReindexingTask {
    private String srcIndexName;
    private String dstIndexName;
    private String indexAlias;
    private String elasticTaskId;
    private boolean tracking;

    public TrackedReindexingTask() {
    }

    public TrackedReindexingTask(String srcIndexName, String dstIndexName, String indexAlias, String elasticTaskId) {
        this.srcIndexName = srcIndexName;
        this.dstIndexName = dstIndexName;
        this.indexAlias = indexAlias;
        this.elasticTaskId = elasticTaskId;
        this.tracking = true;
    }

    public String getSrcIndexName() {
        return srcIndexName;
    }

    public void setSrcIndexName(String srcIndexName) {
        this.srcIndexName = srcIndexName;
    }

    public String getDstIndexName() {
        return dstIndexName;
    }

    public void setDstIndexName(String dstIndexName) {
        this.dstIndexName = dstIndexName;
    }

    public String getIndexAlias() {
        return indexAlias;
    }

    public void setIndexAlias(String indexAlias) {
        this.indexAlias = indexAlias;
    }

    public String getElasticTaskId() {
        return elasticTaskId;
    }

    public void setElasticTaskId(String elasticTaskId) {
        this.elasticTaskId = elasticTaskId;
    }

    public boolean isTracking() {
        return tracking;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
}
