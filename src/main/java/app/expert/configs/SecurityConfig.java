package app.expert.configs;

import app.base.services.GContextService;
import app.expert.db.manager.ManagerCache;
import app.expert.filters.UserAuthorizationFilter;
import app.expert.services.CookieService;
import app.expert.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    private static final String[] NOT_SECURED_POST_URLS = {
            "/auth",

    };
    private static final String[] NOT_SECURED_GET_URLS = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**",
            "/static/manager/statusInfo**",
    };
    
    private static final String[] NOT_SECURED_PUT_URLS = {
            "/static/call"
    };
    
    private static final String[] NOT_SECURED_PATCH_URLS = {
            "/static/manager/statusInfo",
            "/static/call"
    };

    public final static int SESSION_LIFETIME_TIME_SECONDS = 60 * 60;

    private final GContextService context;
    private final CookieService cookieService;
    private final SessionService sessionService;
    private final ManagerCache managerCache;


    @Value("${spring.boot.admin.client.metadata.user.name:admin}")
    private String adminUser;

    @Value("${spring.boot.admin.client.metadata.user.password:th1s1s4dm1n}")
    private String adminPassword;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(HttpMethod.POST, NOT_SECURED_POST_URLS)
                .antMatchers(HttpMethod.GET, NOT_SECURED_GET_URLS)
                .antMatchers(HttpMethod.PUT, NOT_SECURED_PUT_URLS)
                .antMatchers(HttpMethod.PATCH, NOT_SECURED_PATCH_URLS);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        UserAuthorizationFilter clientAuthorizationFilter =
                new UserAuthorizationFilter(authenticationManager(),
                        context, cookieService, sessionService, managerCache);

        http.cors().and().csrf().ignoringAntMatchers().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilter(clientAuthorizationFilter)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}

