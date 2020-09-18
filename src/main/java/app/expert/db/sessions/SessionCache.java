package app.expert.db.sessions;

import app.base.db.GRedisCache;
import org.springframework.stereotype.Component;

@Component
public class SessionCache extends GRedisCache<Long, Session, SessionRepository> {

    public SessionCache(SessionRepository repository) {
        super(repository, Session.class);
    }

    public Session findByManager(Long manager) {
        return getRepository().findByManager(manager);
    }
}
