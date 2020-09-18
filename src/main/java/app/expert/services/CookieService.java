package app.expert.services;

import app.base.exceptions.GException;
import app.base.services.GContextService;
import app.base.utils.JwtUtils;
import app.expert.configs.SecurityConfig;
import app.expert.db.manager.Manager;
import app.expert.db.manager.ManagerCache;
import app.expert.db.sessions.Session;
import app.expert.db.sessions.SessionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class CookieService {

    public static final String AUTH_COOKIE_NAME = "USER_TOKEN";

    private final GContextService context;
    private final ManagerCache managerCache;
    private final SessionCache sessionCache;

    private void setCookie(HttpServletResponse response, String name, String value, int age) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(age);
        response.addCookie(cookie);
    }

    private void setHeaderToken(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.AUTHORIZATION, token);
    }

    public void setUserCookieExpired(HttpServletResponse response) throws GException {
        setCookie(response, AUTH_COOKIE_NAME, "expired", 0);
    }

    public void setUserCookie(HttpServletResponse response) throws GException {
        Manager manager = managerCache.find(Long.parseLong(context.getUser()));
        Session session = sessionCache.find(context.getSession());
        String token = JwtUtils.generateUserToken(manager.getAgentId(), session.getExpired(), context.getUserTokenPairs());
        setCookie(response, AUTH_COOKIE_NAME, token, SecurityConfig.SESSION_LIFETIME_TIME_SECONDS);

        setHeaderToken(response, token);
    }
}
