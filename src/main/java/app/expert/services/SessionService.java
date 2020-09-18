package app.expert.services;

import app.base.exceptions.GException;
import app.base.exceptions.GNotFound;
import app.base.services.GContextService;
import app.expert.configs.SecurityConfig;
import app.expert.constants.ExpertErrors;
import app.expert.db.manager.Manager;
import app.expert.db.manager.ManagerCache;
import app.expert.db.sessions.Session;
import app.expert.db.sessions.SessionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Component
public class SessionService {

    private final SessionCache sessionCache;
    private final ManagerCache managerCache;
    private final GContextService context;

    private void pushSessionToContext(Session session) {
        context.setSession(session.getId());
        context.setSessionExpired(session.getExpired());
        context.setUser(session.getManager());
    }

    private void create(Manager manager) {
        Date date = new Date();
        pushSessionToContext((sessionCache.save(Session.builder()
                .created(date)
                .expired(new Date(date.getTime() + SecurityConfig.SESSION_LIFETIME_TIME_SECONDS * 1000L))
                .manager(manager.getId())
                .build())));
    }

    private void update(Manager manager) throws GNotFound {
        Session session = sessionCache.getRepository().findFirstByManagerAndDisabledIsNull(manager.getId())
                .orElseThrow(() -> new GNotFound(ExpertErrors.SESSION_NOT_FOUND));
        session.setExpired(new Date(new Date().getTime() + SecurityConfig.SESSION_LIFETIME_TIME_SECONDS * 1000L));
        sessionCache.save(session);
        pushSessionToContext(session);
    }

    public void createOrUpdateSession(Manager manager) {
        try {
            update(manager);
        } catch (GNotFound e) {
            create(manager);
        }
    }

    public Session getSession() throws GException {
        return sessionCache.find(context.getSession());
    }

    public Session closeSession() throws GException {
        Session session = sessionCache.find(context.getSession());
        session.setExpired(new Date());
        return sessionCache.save(session);
    }

    public Long addStationToSession(String agentIg, Long stationId) throws GException {
        // проверить что агент существует, если нет то метод выкинет ошибку
        Manager manager = managerCache.findByAgentId(agentIg);

        // найти сессию по пользователю
        Session session = sessionCache.findByManager(manager.getId());
        if (session == null) throw new GNotFound(ExpertErrors.SESSION_NOT_FOUND);

        // проверить что сессия не истекла
        if (session.getExpired() != null && (new Date()).compareTo(session.getExpired()) >= 0) {
            throw new GException(ExpertErrors.SESSION_EXPIRED);
        }

        // добавить stationId в сессию и сохранить
        session.setStation(stationId);
        sessionCache.save(session);
        return stationId;
    }
}
