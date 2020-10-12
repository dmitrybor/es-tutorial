package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Internal user search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.internal-user")
public class InternalUserSearchProperties extends EntitySearchProperties {

}
