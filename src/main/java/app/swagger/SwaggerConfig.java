package app.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.core.SpringVersion;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

public class SwaggerConfig {

    private final Contact CONTACT = new Contact(
            "Dias Yesbay",
            "tel:+77714056677",
            "d.esbay@g-lab.ru");

    @Value("${info.app.name}")
    private String system;

    @Value("${info.app.description}")
    private String description;

    @Value("${info.app.version}")
    private String version;


    protected SecurityScheme getBasicAuthScheme() {
        return new BasicAuth("Basic");
    }

    protected SecurityScheme getBearerAuthScheme() {
        return new ApiKey("Bearer", HttpHeaders.AUTHORIZATION, "header");
    }

    protected List<? extends SecurityScheme> getSecuritySchemes() {
        return Collections.singletonList(getBearerAuthScheme());
    }

    private ApiInfo metaData() {

        description = description + "<br/> " +
                "Spring " + SpringVersion.getVersion() + "<br/>" +
                "Spring Boot " + SpringBootVersion.getVersion() + "<br/>";

        return new ApiInfo(
                system,
                description,
                version,
                null,
                CONTACT,
                null,
                null,
                Collections.emptyList());
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("app"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .securitySchemes(getSecuritySchemes());
    }

}
