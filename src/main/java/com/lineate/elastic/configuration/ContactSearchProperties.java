package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contact search properties.
 */
@Component
@ConfigurationProperties(prefix = "search.contact")
public class ContactSearchProperties extends EntitySearchProperties {

}
