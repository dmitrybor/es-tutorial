package com.lineate.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "search.product")
public class ProductSearchProperties extends EntitySearchProperties {

}
