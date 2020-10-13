package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Company search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.company")
public class CompanySearchProperties extends EntitySearchProperties {

}
