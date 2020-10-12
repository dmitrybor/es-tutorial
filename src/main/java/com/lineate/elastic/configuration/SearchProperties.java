package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Search properties.
 */
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    private String host;
    private int port;
    private int batchSize;
    private String trackingTaskRequestInterval;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getTrackingTaskRequestInterval() {
        return trackingTaskRequestInterval;
    }

    public void setTrackingTaskRequestInterval(String trackingTaskRequestInterval) {
        this.trackingTaskRequestInterval = trackingTaskRequestInterval;
    }
}

