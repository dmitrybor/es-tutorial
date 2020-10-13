package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Group search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.group")
public class GroupSearchProperties extends EntitySearchProperties {

}
