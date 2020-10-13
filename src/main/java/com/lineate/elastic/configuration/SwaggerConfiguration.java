package com.lineate.elastic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Profile("swagger")
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.lineate.elastic"))
                .paths(PathSelectors.ant("/api/**"))
                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiDetails());
    }

//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .securityContexts(Lists.newArrayList(securityContext()))
//                .securitySchemes(Lists.newArrayList(apiKey()))
//                .select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.ant("/api/**"))
//                .build()
//                .apiInfo(apiInfo());
//    }

    private ApiInfo apiDetails() {
        return new ApiInfo(
                "Elastic index management application",
                "Application for creating indexes and reindexing",
                "0.0.1",
                "Free to use",
                new Contact("Dmitry Borisenko", "https://www.lineate.com", "dmitry_borisenko@lineate.com"),
                "License: GNU GPLv3",
                "https://www.gnu.org/licenses/gpl-3.0.en.html",
                Collections.emptyList());
    }
}
