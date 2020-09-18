package app.expert.controllers;

import app.base.constants.GErrors;
import app.base.controllers.GControllerAdvice;
import app.base.exceptions.GException;
import app.base.models.GResponse;
import app.expert.db.manager.Manager;
import app.expert.db.statics.managerRole.ManagerRoleCache;
import app.expert.models.RqAuth;
import app.expert.models.RsAuth;
import app.expert.services.AuthenticationService;
import app.expert.services.CookieService;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Api(tags = "1015. Авторизация")
public class Authorization extends GControllerAdvice {

    private final AuthenticationService authenticationService;
    private final CookieService cookieService;
    private final ManagerRoleCache managerRoleCache;

    @PostMapping("/auth")
    private RsAuth auth(@RequestBody RqAuth auth, HttpServletResponse response) throws GException {
        Manager manager = authenticationService.auth(auth.getUsername(), auth.getPassword());
        cookieService.setUserCookie(response);
        return RsAuth.get(manager, managerRoleCache, response.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/auth/logout")
    private GResponse logout(HttpServletResponse response) throws GException {
        authenticationService.logout();
        cookieService.setUserCookieExpired(response);
        return getResponse(GErrors.OK);
    }
}
