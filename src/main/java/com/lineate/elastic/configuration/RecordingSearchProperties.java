package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Recording search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.recording")
public class RecordingSearchProperties extends EntitySearchProperties {

}
