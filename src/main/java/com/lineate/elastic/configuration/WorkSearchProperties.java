package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Work search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.work")
public class WorkSearchProperties extends EntitySearchProperties {

}
