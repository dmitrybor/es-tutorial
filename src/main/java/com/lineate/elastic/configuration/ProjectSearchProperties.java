package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Project search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.project")
public class ProjectSearchProperties extends EntitySearchProperties {

}
