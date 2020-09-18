package app.expert.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.springframework.web.cors.CorsConfiguration.ALL;

@Configuration
public class CorsConfig {

    @Value("#{'${spring.application.domains:*}'.split(',')}")
    private List<String> domains;

    @Value("#{'${spring.application.methods:*}'.split(',')}")
    private List<String> methods;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(domains);
        config.setAllowedMethods(methods);
        config.addAllowedOrigin(ALL);
        config.addAllowedMethod(ALL);
        config.addAllowedHeader(ALL);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
