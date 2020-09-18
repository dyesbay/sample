package app.base.configs;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
//@EnableConfigurationProperties
//@ConfigurationProperties("zuul")
public class ZuulConfigs {

    private Map<String, Map> routes;

    public Map<String, Map> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Map> routes) {
        this.routes = routes;
    }
}
