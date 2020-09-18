package app.expert.filters;

import app.base.constants.GConstants;
import app.base.constants.GErrors;
import app.base.exceptions.*;
import app.base.models.GResponse;
import app.base.objects.IGEnum;
import app.base.services.GContextService;
import app.base.utils.*;
import app.expert.constants.ExpertErrors;
import app.expert.db.manager.Manager;
import app.expert.db.manager.ManagerCache;
import app.expert.db.sessions.Session;
import app.expert.services.CookieService;
import app.expert.services.SessionService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class UserAuthorizationFilter extends BasicAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthorizationFilter.class);

    private final GContextService context;
    private final CookieService cookieService;
    private final SessionService sessionService;
    private final ManagerCache managerCache;
//    private final RoleCache roleCache;

    public UserAuthorizationFilter(AuthenticationManager authManager,
                                   GContextService context,
                                   CookieService cookieService,
                                   SessionService sessionService,
                                   ManagerCache managerCache) {
        super(authManager);
        this.context = context;
        this.cookieService = cookieService;
        this.sessionService = sessionService;
        this.managerCache = managerCache;
//        this.roleCache = roleCache;
        logger.info("Initializing filter [40+]: {}", this.getClass().getSimpleName());
    }

    @Override
    public void destroy() {
        logger.warn("Destructing filter :{}", this.getClass().getSimpleName());
    }

    private String getResponse(String code, String message) {
        return SerializationUtils.toJson(GResponse.builder().code(code).message(message).build());
    }

    protected void fillErrorResponse(HttpServletResponse response, int status, IGEnum error) throws IOException {
        context.setErrorCode(error.name());
        response.setStatus(status);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(getResponse(error.name(), error.getValue()));
    }

    protected void springSecurityContextAuth(String principal) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    protected void springAdminSecurityContextAuth(String principal, List<SimpleGrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private Claims parseClaims(String token) throws GUnauthorized {
        return JwtUtils.getUserClaims(token);
    }

    private Claims getTokenClaims(HttpServletRequest request) throws GUnauthorized {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        Cookie[] cookies = request.getCookies();
        if (cookies != null && token==null) {
            for (Cookie cookie : cookies) {
                if (CookieService.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        if (ObjectUtils.isBlank(token)) throw new GUnauthorized();

        return parseClaims(token);
    }

    private void injectClaimsToContext(Claims claims) throws GBadRequest {
        if (claims.containsKey(GConstants.HEADER_SESSION))
            context.setSession(ObjectUtils.parseLongOrNull(EncryptionUtils.decrypt(claims.get(GConstants.HEADER_SESSION, String.class))));
        if (claims.containsKey(GConstants.HEADER_USER))
            context.setUser(ObjectUtils.parseLongOrNull(EncryptionUtils.decrypt(claims.get(GConstants.HEADER_USER, String.class))));

    }

    private void doFilterUser(HttpServletRequest request,
                              HttpServletResponse response,
                              FilterChain chain) throws IOException, ServletException {
        long start = System.currentTimeMillis();

        Manager manager = null;
        Session session = null;

        try {
            injectClaimsToContext(getTokenClaims(request));

            try {
                session = sessionService.getSession();
            } catch (GNotFound e) {
                throw new GUnauthorized(e.getCode());
            }

            manager = managerCache.find(session.getManager());

            sessionService.createOrUpdateSession(manager);
            cookieService.setUserCookie(response);

        } catch (GNotFound | GNotAllowed | GBadRequest ex) {
            logger.error(ex.getMessage(), ex);
            logger.info(ExceptionUtils.getFullLog(ex));
            fillErrorResponse(response, getStatus(ex), ex.getCode());
//            springSecurityContextAuth("anonymous");
            return;
        } catch (GUnauthorized ex) {
            logger.error(ex.getMessage(), ex);
            logger.info(ExceptionUtils.getFullLog(ex));

            fillErrorResponse(response, getStatus(ex), ExpertErrors.UNAUTHORIZED);
//            springSecurityContextAuth("anonymous");
            return;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.info(ExceptionUtils.getFullLog(ex));
            fillErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), GErrors.INTERNAL_SERVER_ERROR);
            return;
        }

//        List<SimpleGrantedAuthority> list = new ArrayList<>();
//        list.add(new SimpleGrantedAuthority("anonymous"));
//        if (manager != null) {
//            springAdminSecurityContextAuth(manager.getAgentId(), list);
//        }
        springSecurityContextAuth("anonymous");
        chain.doFilter(request, response); // переход на след фильтр
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        doFilterUser(request, response, chain);
    }

    protected int getStatus(GException ex) {
        if (ex instanceof GBadRequest || ex instanceof GAlreadyExists) {
            return HttpStatus.BAD_REQUEST.value();
        } else if (ex instanceof GUnauthorized) {
            return HttpStatus.UNAUTHORIZED.value();
        } else if (ex instanceof GNotFound) {
            return HttpStatus.NOT_FOUND.value();
        } else if (ex instanceof GNotAllowed) {
            return HttpStatus.METHOD_NOT_ALLOWED.value();
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }
}
